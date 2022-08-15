package app.brucehsieh.inventorymanageeer.data.preferences

import android.content.Context
import android.content.SharedPreferences
import app.brucehsieh.inventorymanageeer.common.extension.empty

/**
 * Store Walmart related preferences.
 * */
class WalmartPreferences(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun putToken(token: String) {
        edit { putString(KEY_TOKEN, token) }
    }

    fun putTokenExpirationTime(time: Long) {
        edit { putLong(KEY_TOKEN_EXPIRATION_TIME, time) }
    }

    fun putTokenType(tokenType: String) {
        edit { putString(KEY_TOKEN_TYPE, tokenType) }
    }

    fun getToken(): String {
        return preferences.getString(KEY_TOKEN, String.empty()).orEmpty()
    }

    fun getTokenExpirationTime(): Long {
        return preferences.getLong(KEY_TOKEN_EXPIRATION_TIME, -1)
    }

    fun getTokenType(): String {
        return preferences.getString(KEY_TOKEN_TYPE, String.empty()).orEmpty()
    }

    fun deleteTokenInfo() {
        edit {
            remove(KEY_TOKEN)
            remove(KEY_TOKEN_EXPIRATION_TIME)
            remove(KEY_TOKEN_TYPE)
        }
    }

    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
        with(preferences.edit()) {
            block()
            commit()
        }
    }

    companion object {
        const val PREFERENCES_NAME = "WAR_MART_PREFERENCES"
        const val KEY_TOKEN = "token"
        const val KEY_TOKEN_EXPIRATION_TIME = "tokenExpirationTime"
        const val KEY_TOKEN_TYPE = "tokenType"
    }
}