package app.brucehsieh.inventorymanageeer.ui.inventory

import android.util.Log
import androidx.lifecycle.*
import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.data.remote.serviceapi.ShopifyApiService
import app.brucehsieh.inventorymanageeer.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.model.BaseListing
import app.brucehsieh.inventorymanageeer.model.ShopifyListing
import app.brucehsieh.inventorymanageeer.model.WalmartListing
import app.brucehsieh.inventorymanageeer.ui.store.StoreList
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

private const val TAG = "InventoryViewModel"

class InventoryViewModel : ViewModel() {

    private var updateInventoryJob: Job? = null

    private val _currentStore = MutableLiveData<StoreList>()
    private val _walmartListings = MutableLiveData<List<BaseListing>>()
    private val _shopifyListings = MutableLiveData<List<BaseListing>>()

    /**
     * [productListings] can be null because
     * */
    val productListings: LiveData<List<BaseListing>?>
        get() = _currentStore.switchMap {
            when (it) {
                StoreList.Walmart -> _walmartListings
                StoreList.Shopify -> _shopifyListings
                else -> throw IllegalArgumentException("No such Store exists.")
            }
        }

    var currentSelectedListing: BaseListing? = null
        private set

    /**
     * Update the [_currentStore].
     * */
    fun onStoreChange(position: Int) {
        val storeName = StoreList.values()[position]
        _currentStore.value = storeName

        when (storeName) {
            StoreList.Walmart -> getWalmartItems()
            StoreList.Shopify -> getShopifyItems()
        }
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
    private fun getShopifyItems() {
        viewModelScope.launch {
            try {
                val shopifyItems = ShopifyApiService.getItems()

                // Nothing in our store listing
                if (shopifyItems.products.isEmpty()) return@launch

                // TODO: Handle variants
                _shopifyListings.value = shopifyItems.products.map {
                    ShopifyListing(
                        productName = it.title,
                        productSku = it.variants.first().sku,
                        quantity = it.variants.first().inventoryQuantity,
                        price = it.variants.first().price.toFloat(),
                        imageUrl = it.image.src
                    )
                }

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
    private fun getWalmartItems() {

        if (_walmartListings.value?.isNotEmpty() == true) return

        viewModelScope.launch {
            try {
                val walmartItems = WalmartApiService.getItems()

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
                        val (quantity, _) = WalmartApiService.getInventoryBySku(sku = it.productSku)
                        (it as WalmartListing).copy(quantity = quantity.amount)
                    }
                }?.awaitAll()

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
                val newWalmartInventory = WalmartApiService.updateInventoryBySku(sku, newQuantity)

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
}