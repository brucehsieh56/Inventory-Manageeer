package app.brucehsieh.inventorymanageeer.storefront.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.common.domain.model.WalmartListing
import app.brucehsieh.inventorymanageeer.common.presentation.OneTimeEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class WalmartViewModel @Inject constructor(
    private val walmartApiService: WalmartApiService,
) : ViewModel() {

    private val _uiState = MutableLiveData(InventoryUiState())
    val uiState: LiveData<InventoryUiState> get() = _uiState

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
    fun onInventoryUpdateBySku(sku: String, newQuantity: Int) {
        updateInventoryJob?.cancel()
        updateInventoryJob = viewModelScope.launch {
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