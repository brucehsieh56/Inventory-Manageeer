package app.brucehsieh.inventorymanageeer.common.data.remote.interceptors

import android.os.Build
import app.brucehsieh.inventorymanageeer.common.data.preferences.Preferences
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.AUTH_ENDPOINT
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.ITEMS_ENDPOINT
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.TOKEN_TYPE
import app.brucehsieh.inventorymanageeer.common.data.remote.serviceapi.WalmartApiService.Companion.WALMART_ACCESS_TOKEN_KEY
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.Instant

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class AuthenticationInterceptorTest {

    private lateinit var walmartPreferences: Preferences
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authenticationInterceptor: AuthenticationInterceptor
    private lateinit var okHttpClient: OkHttpClient

    private val endpointSeparator = "/"
    private val authEndPoint = "$endpointSeparator$AUTH_ENDPOINT"
    private val itemsEndPoint = "$endpointSeparator$ITEMS_ENDPOINT"

    private val validToken = "validToken"
    private val expiredToken = "expiredToken"
    private val validTokenJson = """
                        {
                          "token_type": "Bearer",
                          "expires_in": 9000,
                          "access_token": "validToken"
                        }
                    """.trimIndent()

    @Before
    fun setUp() {
        walmartPreferences = mock(Preferences::class.java)
        mockWebServer = MockWebServer()
        mockWebServer.start(8080)

        authenticationInterceptor = AuthenticationInterceptor(walmartPreferences)
        okHttpClient = OkHttpClient.Builder().addInterceptor(authenticationInterceptor).build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun authenticationInterceptor_validToken() {
        // Arrange
        `when`(walmartPreferences.getToken()).thenReturn(validToken)
        `when`(walmartPreferences.getTokenExpirationTime()).thenReturn(
            Instant.now().plusSeconds(60 * 15).epochSecond
        )

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    itemsEndPoint -> MockResponse().setResponseCode(200)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        // Act
        okHttpClient.newCall(
            Request.Builder().url(mockWebServer.url(ITEMS_ENDPOINT)).build()
        ).execute()

        // Assert
        val request = mockWebServer.takeRequest()
        with(request) {
            assertEquals(method, "GET")
            assertEquals(path, itemsEndPoint)
            assertEquals(getHeader(WALMART_ACCESS_TOKEN_KEY), validToken)
        }
    }

    @Test
    fun authenticationInterceptor_tokenExpired_tokenRefreshed() {
        // Arrange
        `when`(walmartPreferences.getToken()).thenReturn(expiredToken)
        `when`(walmartPreferences.getTokenExpirationTime()).thenReturn(
            Instant.now().minusSeconds(60 * 15).epochSecond
        )

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when (request.path) {
                    authEndPoint -> MockResponse().setResponseCode(200).setBody(validTokenJson)
                    itemsEndPoint -> MockResponse().setResponseCode(200)
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }

        // Act
        okHttpClient.newCall(
            Request.Builder().url(mockWebServer.url(ITEMS_ENDPOINT)).build()
        ).execute()

        // Assert
        val tokenRequest = mockWebServer.takeRequest()
        val itemsRequest = mockWebServer.takeRequest()

        with(tokenRequest) {
            assertEquals(method, "POST")
            assertEquals(path, authEndPoint)
        }

        val inOrder = inOrder(walmartPreferences)
        inOrder.verify(walmartPreferences).getToken()
        inOrder.verify(walmartPreferences).putToken(validToken)

        verify(walmartPreferences, times(1)).getToken()
        verify(walmartPreferences, times(1)).putToken(validToken)
        verify(walmartPreferences, times(1)).getTokenExpirationTime()
        verify(walmartPreferences, times(1)).putTokenExpirationTime(anyLong())
        verify(walmartPreferences, times(1)).putTokenType(TOKEN_TYPE)
        verifyNoMoreInteractions(walmartPreferences)

        with(itemsRequest) {
            assertEquals(method, "GET")
            assertEquals(path, itemsEndPoint)
            assertEquals(getHeader(WALMART_ACCESS_TOKEN_KEY), validToken)
        }
    }
}