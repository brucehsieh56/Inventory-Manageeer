package app.brucehsieh.inventorymanageeer.ui.inventory

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.common.data.preferences.WalmartPreferences
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.AuthenticationInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.NetworkStatusInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.ShopifyApiService
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.common.utils.NetworkHelper
import app.brucehsieh.inventorymanageeer.model.BaseListing
import app.brucehsieh.inventorymanageeer.model.ShopifyListing
import app.brucehsieh.inventorymanageeer.model.WalmartListing
import app.brucehsieh.inventorymanageeer.ui.state.InventoryViewState
import app.brucehsieh.inventorymanageeer.ui.store.StoreList
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.net.SocketTimeoutException

private const val TAG = "InventoryViewModel"

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private var updateInventoryJob: Job? = null
    private var getItemsJob: Job? = null

    /**
     * State that contains UI-related data and settings.
     * */
    private val _inventoryViewState = MutableLiveData<InventoryViewState>()
    val inventoryViewState: LiveData<InventoryViewState> get() = _inventoryViewState.distinctUntilChanged()

    private val _walmartListings = MutableLiveData<List<BaseListing>>()
    private val _shopifyListings = MutableLiveData<List<BaseListing>>()

    var currentSelectedListing: BaseListing? = null
        private set

    /**
     * [productListings] can be null because
     * */
    val productListings: LiveData<List<BaseListing>?>
        get() = _inventoryViewState.switchMap { state ->
            when (state.currentStore) {
                StoreList.Walmart -> {
                    _walmartListings.value = if (state.isAscending) {
                        _walmartListings.value?.sortedBy { it.productName }
                    } else {
                        _walmartListings.value?.sortedByDescending { it.productName }
                    }
                    _walmartListings
                }
                StoreList.Shopify -> {
                    _shopifyListings.value = if (state.isAscending) {
                        _shopifyListings.value?.sortedBy { it.productName }
                    } else {
                        _shopifyListings.value?.sortedByDescending { it.productName }
                    }
                    _shopifyListings
                }
            }
        }

    private val networkHelper by lazy { NetworkHelper(application.applicationContext) }
    private val walmartPreferences by lazy { WalmartPreferences(application.applicationContext) }
    private val okHttpClient = OkHttpClient.Builder().apply {
        val networkStatusInterceptor = NetworkStatusInterceptor(networkHelper)
        val authenticationInterceptor = AuthenticationInterceptor(walmartPreferences)
        val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC)

        addInterceptor(networkStatusInterceptor)
        addInterceptor(authenticationInterceptor)
        addInterceptor(loggingInterceptor)
    }.build()
    private val walmartApiService by lazy { WalmartApiService(okHttpClient) }

    /**
     * Update the [_inventoryViewState].
     * */
    fun onStoreChange(position: Int) {
        val storeName = StoreList.values()[position]

        // Return if the store name is the same as current store
        if (storeName == _inventoryViewState.value?.currentStore) return

        _inventoryViewState.value = InventoryViewState(
            currentStore = storeName,
            isAscending = _inventoryViewState.value?.isAscending ?: true
        )

//        when (storeName) {
//            StoreList.Walmart -> getWalmartItems()
//            StoreList.Shopify -> getShopifyItems()
//        }
    }

    fun sorting(isAscending: Boolean) {
        _inventoryViewState.value = _inventoryViewState.value?.copy(isAscending = isAscending)
    }

    /**
     * Run listing sorting.
     * */
    private fun runSorting() {
        _inventoryViewState.value = _inventoryViewState.value?.copy()
    }

    /**
     * Cache the current selected listing.
     * */
    fun updateCurrentSelected(listing: BaseListing) {
        currentSelectedListing = listing
    }

    /**
     * Get Shopify product listings.
     * */
    fun getShopifyItems() {

        if (_shopifyListings.value?.isNotEmpty() == true) return

        getItemsJob?.cancel()
        getItemsJob = viewModelScope.launch {
            try {
                val shopifyItems = ShopifyApiService.getItems()

                // Nothing in our store listing
                if (shopifyItems.products.isEmpty()) return@launch

                /**
                 * Create a [BaseListing] for each product variants.
                 * */
                val tempShopifyListings = mutableListOf<BaseListing>()
                shopifyItems.products.forEach { product ->

                    // This product has variants
                    product.variants.forEach { variant ->
                        tempShopifyListings.add(
                            ShopifyListing(
                                productName = "${product.title} - ${variant.title}",
                                productSku = variant.sku,
                                quantity = variant.inventoryQuantity,
                                price = variant.price.toFloat(),
                                imageUrl = if (variant.imageId == null) {
                                    // no product variants, use default image, which could be null
                                    product.image?.src ?: String.empty()
                                } else {
                                    // has product variants, use variant's first image
                                    product.images.first { image ->
                                        image.id == variant.imageId
                                    }.src
                                },
                                inventoryItemId = variant.inventoryItemId
                            )
                        )
                    }
                }

                _shopifyListings.value = tempShopifyListings

                runSorting()
            } catch (t: CancellationException) {
                Log.i(TAG, "getItems: Coroutine cancelled")
            } catch (t: SocketTimeoutException) {
                Log.i(TAG, "getItems: SocketTimeoutException")
                t.printStackTrace()
            } catch (t: Failure.ServerError) {
                Log.i(TAG, "getItems: ServerError ${t.code} ${t.message} ${t.cause}")
                t.printStackTrace()
            } catch (t: Throwable) {
                Log.i(TAG, "getItems: Throwable")
                t.printStackTrace()
            }
        }
    }

    /**
     * Get Walmart product listings.
     * */
    fun getWalmartItems() {

        if (_walmartListings.value?.isNotEmpty() == true) return

        getItemsJob?.cancel()
        getItemsJob = viewModelScope.launch {
            try {
                val walmartItems = walmartApiService.getItems()

                // Nothing in our Walmart listing
                if (walmartItems.totalItems == 0) return@launch

                _walmartListings.value = walmartItems.ItemResponse.map {
                    WalmartListing(
                        productName = it.productName,
                        productSku = it.sku,
                        quantity = -1,
                        price = it.price.amount.toFloat()
                    )
                }

                // Get inventory
                _walmartListings.value = _walmartListings.value?.map {
                    async {
                        val (quantity, _) = walmartApiService.getInventoryBySku(sku = it.productSku)
                        (it as WalmartListing).copy(quantity = quantity.amount)
                    }
                }?.awaitAll()

                /**
                 * Enforce sorting after GET inventory.
                 * */
                runSorting()
            } catch (t: CancellationException) {
                Log.i(TAG, "getItems: Coroutine cancelled")
            } catch (t: SocketTimeoutException) {
                Log.i(TAG, "getItems: SocketTimeoutException")
                t.printStackTrace()
            } catch (t: Failure.ServerError) {
                Log.i(TAG, "getItems: ServerError ${t.code} ${t.message} ${t.cause}")
                t.printStackTrace()
            } catch (t: Throwable) {
                Log.i(TAG, "getItems: Throwable")
                t.printStackTrace()
            }
        }
    }

    /**
     * Update inventory.
     *
     * Cancel [updateInventoryJob] before launching multiple coroutines.
     * */
    fun updateInventoryBySku(sku: String, newQuantity: Int, delayMillis: Long = 100L) {
        updateInventoryJob?.cancel()
        updateInventoryJob = viewModelScope.launch {

            // Avoid firing too many requests in a short period
            delay(delayMillis)

            try {
                val newWalmartInventory = walmartApiService.updateInventoryBySku(sku, newQuantity)

                _walmartListings.value = _walmartListings.value?.map {
                    if (it.productSku == newWalmartInventory.sku) {
                        (it as WalmartListing).copy(quantity = newWalmartInventory.quantity.amount)
                    } else {
                        it
                    }
                }
            } catch (t: CancellationException) {
                Log.i(TAG, "updateInventoryBySku: CancellationException")
            } catch (t: SocketTimeoutException) {
                Log.i(TAG, "updateInventoryBySku: SocketTimeoutException")
            } catch (t: Throwable) {
                Log.i(TAG, "updateInventoryBySku: error")
                t.printStackTrace()
            }
        }
    }

    /**
     * Update Shopify inventory.
     * */
    fun updateShopifyInventory(inventoryItemId: Long, newQuantity: Int) {
        updateInventoryJob?.cancel()
        updateInventoryJob = viewModelScope.launch {
            try {
                val shopifyInventoryLevel = ShopifyApiService.getSingleInventory(inventoryItemId)

                val inventoryLevel = shopifyInventoryLevel.inventory_levels.first()

                val newInventoryLevel = ShopifyApiService.updateSingleInventory(
                    inventoryItemId = inventoryItemId,
                    locationId = inventoryLevel.locationId,
                    newQuantity = newQuantity
                )

                _shopifyListings.value = _shopifyListings.value?.map {
                    if ((it as ShopifyListing).inventoryItemId == newInventoryLevel.inventory_level.inventoryItemId) {
                        it.copy(quantity = newInventoryLevel.inventory_level.available)
                    } else {
                        it
                    }
                }
            } catch (t: CancellationException) {
                Log.i(TAG, "updateInventoryBySku: CancellationException")
            } catch (t: SocketTimeoutException) {
                Log.i(TAG, "updateInventoryBySku: SocketTimeoutException")
            } catch (t: Throwable) {
                Log.i(TAG, "updateInventoryBySku: error")
                t.printStackTrace()
            }
        }
    }
}