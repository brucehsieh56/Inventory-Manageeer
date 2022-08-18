package app.brucehsieh.inventorymanageeer.storefront.presentation

import app.brucehsieh.inventorymanageeer.common.domain.model.BaseListing

/**
 * UI data for inventory fragments.
 * */
data class InventoryUiState(
    val listings: List<BaseListing> = emptyList(),
    val error: Throwable? = null,
)
