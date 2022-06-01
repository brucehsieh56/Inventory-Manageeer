package app.brucehsieh.inventorymanageeer.common.exception

/**
 * Custom [Throwable].
 * */
sealed class Failure : Throwable() {
    data class ServerError(
        val code: Int,
        override val message: String,
        val description: String
    ) : Failure()
}
