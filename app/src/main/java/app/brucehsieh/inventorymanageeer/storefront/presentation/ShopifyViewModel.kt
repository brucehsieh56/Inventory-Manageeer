package app.brucehsieh.inventorymanageeer.storefront.presentation

import androidx.lifecycle.*
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.ShopifyApiService
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.common.domain.model.ShopifyListing
import app.brucehsieh.inventorymanageeer.common.presentation.OneTimeEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShopifyViewModel @Inject constructor(
    private val shopifyApiService: ShopifyApiService,
) : ViewModel() {

    private val _uiState = MutableLiveData(InventoryUiState())
    val uiState: LiveData<InventoryUiState> get() = _uiState

    private var updateInventoryJob: Job? = null

    init {
        onListingLoad()
    }

    fun onListingLoad() {
        viewModelScope.launch {
            try {
                // Reset
                _uiState.value = uiState.value?.copy(listings = emptyList(), isLoading = true)

                val shopifyItems = shopifyApiService.getItems()

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
                _uiState.value = uiState.value?.copy(error = OneTimeEvent(t))
            } finally {
                _uiState.value = uiState.value?.copy(isLoading = false)
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
                val shopifyInventoryLevel = shopifyApiService.getSingleInventory(inventoryItemId)

                val inventoryLevel = shopifyInventoryLevel.inventory_levels.first()

                val newInventoryLevel = shopifyApiService.updateSingleInventory(
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
                _uiState.value = uiState.value?.copy(error = OneTimeEvent(t))
            }
        }
    }
}