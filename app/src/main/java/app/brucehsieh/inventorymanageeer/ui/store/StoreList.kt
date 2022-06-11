package app.brucehsieh.inventorymanageeer.ui.store

import app.brucehsieh.inventorymanageeer.R

/**
 * List of all the market places. Use [position] as an index to access the market place.
 * */
enum class StoreList(val position: Int) {
    Walmart(position = 0), Shopify(position = 1);


    companion object {
        /**
         * Get the corresponding drawable.
         * */
        fun getIconDrawable(storeName: StoreList): Int {
            return when (storeName) {
                Walmart -> R.drawable.ic_market_place_walmart
                Shopify -> R.drawable.ic_market_place_shopify
            }
        }
    }
}