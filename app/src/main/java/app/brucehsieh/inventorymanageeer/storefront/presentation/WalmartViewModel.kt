package app.brucehsieh.inventorymanageeer.storefront.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.brucehsieh.inventorymanageeer.common.data.preferences.WalmartPreferences
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.AuthenticationInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.NetworkStatusInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.interceptors.SocketTimeoutInterceptor
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.common.utils.NetworkHelper
import app.brucehsieh.inventorymanageeer.common.domain.model.WalmartListing
import app.brucehsieh.inventorymanageeer.common.presentation.OneTimeEvent
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class WalmartViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(InventoryUiState())
    val uiState: LiveData<InventoryUiState> get() = _uiState

    private val networkHelper by lazy { NetworkHelper(application.applicationContext) }
    private val walmartPreferences by lazy { WalmartPreferences(application.applicationContext) }
    private val okHttpClient = OkHttpClient.Builder().apply {
        val networkStatusInterceptor = NetworkStatusInterceptor(networkHelper)
        val authenticationInterceptor = AuthenticationInterceptor(walmartPreferences)
        val socketTimeoutInterceptor = SocketTimeoutInterceptor()
        val loggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BASIC)

        addInterceptor(networkStatusInterceptor)
        addInterceptor(socketTimeoutInterceptor)
        addInterceptor(authenticationInterceptor)
        addInterceptor(loggingInterceptor)
    }.build()

    private val walmartApiService by lazy { WalmartApiService(okHttpClient) }

    private var updateInventoryJob: Job? = null

    fun onListingLoad() {
        viewModelScope.launch {
            try {
                val walmartItems = walmartApiService.getItems()

                // Nothing in our Walmart listing
                if (walmartItems.totalItems == 0) return@launch

                val walmartListings = walmartItems.ItemResponse.map {
                    WalmartListing(
                        productName = it.productName,
                        productSku = it.sku,
                        quantity = -1,
                        price = it.price.amount.toFloat()
                    )
                }.map { listing ->
                    // Get inventory
                    async {
                        val (quantity, _) = walmartApiService.getInventoryBySku(sku = listing.productSku)
                        listing.copy(quantity = quantity.amount)
                    }
                }.awaitAll()

                _uiState.value = uiState.value?.copy(listings = walmartListings)
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = uiState.value?.copy(error = OneTimeEvent(t))
            }
        }
    }

    /**
     * Update inventory.
     * */
    fun onInventoryUpdateBySku(sku: String, newQuantity: Int, delayMillis: Long = 100L) {
        updateInventoryJob?.cancel()
        updateInventoryJob = viewModelScope.launch {

            // Avoid firing too many requests in a short period
            delay(delayMillis)

            try {
                val newWalmartInventory = walmartApiService.updateInventoryBySku(sku, newQuantity)

                val updatedListings = uiState.value?.listings!!.map { listing ->
                    if (listing.productSku == newWalmartInventory.sku) {
                        (listing as WalmartListing).copy(quantity = newWalmartInventory.quantity.amount)
                    } else {
                        listing
                    }
                }
                _uiState.value = uiState.value?.copy(listings = updatedListings)
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = uiState.value?.copy(error = OneTimeEvent(t))
            }
        }
    }
}