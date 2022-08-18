package app.brucehsieh.inventorymanageeer.common.domain.model

sealed interface BaseListing {
    val productName: String
    val productSku: String
    val quantity: Int
    val price: Float
    val imageUrl: String?
}