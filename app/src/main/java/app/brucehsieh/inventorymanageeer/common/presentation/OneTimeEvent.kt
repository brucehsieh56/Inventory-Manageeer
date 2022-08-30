package app.brucehsieh.inventorymanageeer.common.presentation

/**
 * One time event.
 * */
data class OneTimeEvent<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Return the content and prevent its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}
