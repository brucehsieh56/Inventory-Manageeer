package app.brucehsieh.inventorymanageeer.storefront.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.ShopifyApiService
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.common.domain.model.ShopifyListing
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ShopifyViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(InventoryUiState())
    val uiState: LiveData<InventoryUiState> get() = _uiState

    private var updateInventoryJob: Job? = null

    fun onListingLoad() {
        viewModelScope.launch {
            try {
                val shopifyItems = ShopifyApiService.getItems()

                // Nothing exists in our store listing
                if (shopifyItems.products.isEmpty()) return@launch

                /**
                 * Create a [ShopifyListing] for each product variants.
                 * */
                val shopifyListings = mutableListOf<ShopifyListing>()
                shopifyItems.products.forEach { product ->

                    // This product has variants
                    product.variants.forEach { variant ->
                        shopifyListings.add(
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

                _uiState.value = uiState.value?.copy(listings = shopifyListings)
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = uiState.value?.copy(error = t)
            }
        }
    }

    /**
     * Update inventory.
     * */
    fun onInventoryUpdate(inventoryItemId: Long, newQuantity: Int) {
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

                val listing = uiState.value?.listings!!.map { listing ->
                    if ((listing as ShopifyListing).inventoryItemId == newInventoryLevel.inventory_level.inventoryItemId) {
                        listing.copy(quantity = newInventoryLevel.inventory_level.available)
                    } else {
                        listing
                    }
                }

                _uiState.value = uiState.value?.copy(listings = listing)
            } catch (t: Throwable) {
                t.printStackTrace()
                _uiState.value = uiState.value?.copy(error = t)
            }
        }
    }
}