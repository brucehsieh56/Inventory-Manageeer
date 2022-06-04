package app.brucehsieh.inventorymanageeer.ui.inventory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.domain.WalmartApiService
import app.brucehsieh.inventorymanageeer.model.WalmartListing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

private const val TAG = "InventoryViewModel"

class InventoryViewModel : ViewModel() {

    private val _walmartListings = MutableLiveData<List<WalmartListing>>()
    val walmartListing: LiveData<List<WalmartListing>> get() = _walmartListings

    init {
        getItems()
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
                _walmartListings.value = walmartListing.value?.map {
                    async {
                        val (quantity, _) = WalmartApiService.getInventoryBySku(sku = it.productSku)
                        it.copy(quantity = quantity.amount)
                    }
                }?.awaitAll()

            } catch (t: CancellationException) {
                Log.i(TAG, "getItems: Coroutine cancelled")
            } catch (t: Failure.ServerError) {
                Log.i(TAG, "getItems: ServerError ${t.code} ${t.message} ${t.cause}")
            } catch (t: SocketTimeoutException) {
                Log.i(TAG, "getItems: SocketTimeoutException")
            } catch (t: Throwable) {
                Log.i(TAG, "getItems: error")
                t.printStackTrace()
            }
        }
    }
}