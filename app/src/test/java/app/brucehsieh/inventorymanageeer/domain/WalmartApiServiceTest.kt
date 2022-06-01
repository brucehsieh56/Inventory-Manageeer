package app.brucehsieh.inventorymanageeer.domain

import app.brucehsieh.inventorymanageeer.common.exception.Failure
import app.brucehsieh.inventorymanageeer.data.remote.dto.walmart.WalmartToken
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.String
import kotlin.test.assertFailsWith

class WalmartApiServiceTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getToken_returnCorrectToken() {
        // Arrange
        val walmartToken = WalmartToken(
            accessToken = "123456aa",
            tokenType = "Bearer",
            expiresIn = 900
        )
        val body = Gson().toJson(walmartToken)
        mockWebServer.enqueue(MockResponse().setBody(body))
        val baseUrl = String.format("http://localhost:%s", "${mockWebServer.port}/")

        runBlocking {

            // Act
            val token = WalmartApiService.getToken(baseUrl = baseUrl)
            val recordedRequest = mockWebServer.takeRequest()

            // Assert
            assertEquals("POST", recordedRequest.method)
            assertEquals("/token", recordedRequest.path)
            assertEquals(token, walmartToken.accessToken)
        }
    }

    @Test
    fun getToken_wrongKey_serverError400Returned() {
        // Arrange
        val key = "598b565e-aaaa-bbbb-bbc9-d77dcb3691c6"
        val secret = "bbbb-598b565e"
        mockWebServer.enqueue(MockResponse().setResponseCode(400))
        val baseUrl = String.format("http://localhost:%s", "${mockWebServer.port}/")

        // Act
        val exception = assertFailsWith(
            exceptionClass = Failure.ServerError::class,
            block = {
                runBlocking {
                    WalmartApiService.getToken(baseUrl = baseUrl, key = key, secret = secret)
                }
            }
        )

        // Assert
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/token", recordedRequest.path)
        assertEquals(exception.code, 400)
        assertEquals(exception.description, "Bad Request")
    }

    // Test real server
    @Test
    fun getToken_realServer_wrongKey_serverError400Returned() {
        // Arrange
        val key = "598b565e-aaaa-bbbb-bbc9-d77dcb3691c6"
        val secret = "bbbb-598b565e"

        // Act
        // Assert
        val exception = assertFailsWith(
            exceptionClass = Failure.ServerError::class,
            block = {
                runBlocking {
                    WalmartApiService.getToken(key = key, secret = secret)
                }
            }
        )

        assertEquals(exception.code, 400)
        assertEquals(exception.description, "Bad Request")
    }

    @Test
    fun getToken_realServer_correctKeyButWrongSecret_serverError401Returned() {
        // Arrange
        val key = "USE CORRECT KEY"
        val secret = "bbbb-598b565e"

        // Act
        // Assert
        val exception = assertFailsWith(
            exceptionClass = Failure.ServerError::class,
            block = {
                runBlocking {
                    WalmartApiService.getToken(
//                        key = key,
                        secret = secret
                    )
                }
            }
        )

        assertEquals(exception.code, 401)
        assertEquals(exception.description, "Unauthorized")
    }
}