package app.brucehsieh.inventorymanageeer.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.brucehsieh.inventorymanageeer.common.extension.empty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DATA_STORE_MARKET_SETTINGS = "DATA_STORE_MARKET_SETTINGS"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_MARKET_SETTINGS)

/**
 * [DataStore] to save marketplace key and secret.
 * */
class MarketPreferences(context: Context) {

    private val walmartKey = stringPreferencesKey("walmartKey")
    private val walmartSecret = stringPreferencesKey("walmartSecret")

    /**
     * Walmart key and secret exposed as flow.
     * */
    val walmartKeyFlow: Flow<Pair<String, String>> = context.dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val key = preferences[walmartKey] ?: String.empty()
            val secret = preferences[walmartSecret] ?: String.empty()
            key to secret
        }

    /**
     * Suspend function to store user Walmart key and secret.
     * */
    suspend fun storeWalmartKey(key: String, secret: String, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[walmartKey] = key
            preferences[walmartSecret] = secret
        }
    }
}