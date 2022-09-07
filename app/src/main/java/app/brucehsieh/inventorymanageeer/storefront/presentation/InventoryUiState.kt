package app.brucehsieh.inventorymanageeer.storefront.presentation

import app.brucehsieh.inventorymanageeer.common.domain.model.BaseListing
import app.brucehsieh.inventorymanageeer.common.presentation.OneTimeEvent

/**
 * UI data for inventory fragments.
 * */
data class InventoryUiState(
    val listings: List<BaseListing> = emptyList(),
    val isLoading: Boolean = false,
    val error: OneTimeEvent<Throwable>? = null,
)
