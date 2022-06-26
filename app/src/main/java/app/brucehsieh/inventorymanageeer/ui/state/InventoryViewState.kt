package app.brucehsieh.inventorymanageeer.ui.state

import app.brucehsieh.inventorymanageeer.ui.store.StoreList

/**
 * UI state for inventory page.
 *
 * @param currentStore is the current store that user is interested in.
 * @param isAscending indicates if the product listings should be sorted ascending or descending.
 * */
data class InventoryViewState(
    val currentStore: StoreList,
    val isAscending: Boolean
)
