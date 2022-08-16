package app.brucehsieh.inventorymanageeer.common.data.remote.dto.walmart

import app.brucehsieh.inventorymanageeer.common.extension.empty
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant

/**
 * DTO for token-based authentication from Walmart Marketplace api.
 * */
data class WalmartToken(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("expires_in") val expiresIn: Int?,
) {
    companion object {
        val INVALID = WalmartToken(String.empty(), String.empty(), -1)
    }

    /**
     * Token expired time from this data class being created.
     * */
    val expiresAt: Long
        get() {
            if (expiresIn == null) return 0L
            val requestedAt = Instant.now()
            return requestedAt.plusSeconds(expiresIn.toLong()).epochSecond
        }

    fun isValid(): Boolean {
        return accessToken != null && accessToken.isNotEmpty() &&
                expiresIn != null && expiresIn >= 0 &&
                tokenType != null && tokenType.isNotEmpty()
    }
}
