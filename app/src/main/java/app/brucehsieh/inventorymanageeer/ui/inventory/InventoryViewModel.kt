package app.brucehsieh.inventorymanageeer.ui.inventory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.domain.WalmartApiService
import app.brucehsieh.inventorymanageeer.model.WalmartListing
import kotlinx.coroutines.*
import java.net.SocketTimeoutException

private const val TAG = "InventoryViewModel"

class InventoryViewModel : ViewModel() {

    private var updateInventoryJob: Job? = null

    private val _walmartListings = MutableLiveData<List<WalmartListing>>()
    val walmartListings: LiveData<List<WalmartListing>> get() = _walmartListings

    var currentSelectedListing: WalmartListing? = null
        private set

    init {
        getItems()
    }

    /**
     * Cache the current selected listing.
     * */
    fun updateCurrentSelected(listing: WalmartListing) {
        currentSelectedListing = listing
    }

    private fun getItems() {
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
                _walmartListings.value = walmartListings.value?.map {
                    async {
                        val (quantity, _) = WalmartApiService.getInventoryBySku(sku = it.productSku)
                        it.copy(quantity = quantity.amount)
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

                _walmartListings.value = walmartListings.value?.map {
                    if (it.productSku == newWalmartInventory.sku) {
                        it.copy(quantity = newWalmartInventory.quantity.amount)
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