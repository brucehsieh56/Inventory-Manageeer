package app.brucehsieh.inventorymanageeer.data.remote.dto.walmart

import com.google.gson.annotations.SerializedName

/**
 * DTO for token-based authentication from Walmart Marketplace api.
 * */
data class WalmartToken(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)
