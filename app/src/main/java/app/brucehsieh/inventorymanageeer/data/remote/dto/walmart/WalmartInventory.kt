package app.brucehsieh.inventorymanageeer.data.remote.dto.walmart

import app.brucehsieh.inventorymanageeer.common.extension.empty

/**
 * A data class to convert data to json back and forth, therefore can be used to update and retrieve
 * product inventory from Walmart.
 * */
data class WalmartInventory(
    val quantity: Quantity,
    val sku: String
) {
    companion object {
        val empty = WalmartInventory(quantity = Quantity(0), sku = String.empty())
    }
}

data class Quantity(
    val amount: Int,
    val unit: String = "EACH"
)