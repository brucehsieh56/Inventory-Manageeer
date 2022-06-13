package app.brucehsieh.inventorymanageeer.model

sealed interface BaseListing {
    val productName: String
    val productSku: String
    val quantity: Int
    val price: Float
    val imageUrl: String?
}