package app.brucehsieh.inventorymanageeer.common.functional

import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.data.NetworkClient
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper suspend function that uses [suspendCancellableCoroutine] to wrap OkHttp async [enqueue]
 * with [Callback].
 * */
suspend fun <T> suspendRequestCall(
    request: Request,
    transform: (String) -> T,
    default: T,
    client: OkHttpClient = NetworkClient.client,
): T = suspendCancellableCoroutine { continuation ->
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: okhttp3.Call, response: Response) {
            when (response.isSuccessful) {
                true -> {
                    if (response.body == null) {
                        continuation.resume(default)
                    } else {
                        val result = transform(response.body!!.string())
                        continuation.resume(result)
                    }
                }
                false -> {
                    throw Failure.ServerError(
                        code = response.code,
                        message = response.body!!.string(),
                        description = when (response.code) {
                            400 -> "Bad Request"
                            401 -> "Unauthorized"
                            else -> String.empty()
                        }
                    )
                }
            }
        }
    })
}