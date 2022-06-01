package app.brucehsieh.inventorymanageeer.ui.inventory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.domain.WalmartApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "InventoryViewModel"

class InventoryViewModel : ViewModel() {

    fun getItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walmartItems = WalmartApiService.getItems()

                walmartItems.ItemResponse.forEach {
                    Log.i(TAG, "getToken: ${it.productName}")
                }
            } catch (t: CancellationException) {

            } catch (t: Failure.ServerError) {
                Log.i(TAG, "getItems: ServerError ${t.code} ${t.message} ${t.cause}")
            } catch (t: Throwable) {
                Log.i(TAG, "getItems: error")
                t.printStackTrace()
            }
        }
    }
}