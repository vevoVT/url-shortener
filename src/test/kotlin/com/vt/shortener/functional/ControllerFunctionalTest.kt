package com.vt.shortener.functional

import com.datastax.oss.driver.api.core.CqlSession
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import javax.inject.Inject
import kotlin.random.Random
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest
class ControllerFunctionalTest {

    @Inject
    @field:Client("/url_shortener/v1")
    lateinit var client: RxHttpClient

    @Inject
    lateinit var cqlSession: CqlSession

    @Inject
    lateinit var objectMapper: ObjectMapper

    @AfterEach
    fun reset() {
        cqlSession.execute("TRUNCATE test")
    }

    private val randomUrl: String = "https://some-random-url.com"
    private val payload: String = """
        {
            "url": "$randomUrl"
        }
        """.trimIndent()

    private fun getCall(url: String): HttpResponse<String> =
        client.toBlocking().exchange(HttpRequest.GET<Any>(url), String::class.java)

    private fun postCall(): HttpResponse<String> =
        client.toBlocking().exchange(HttpRequest.POST<Any>("/shorten", payload), String::class.java)

    private fun toList(body: String?): List<Map<Any, Any>> =
        objectMapper.readValue(body.toString())

    @Test
    fun `when no data, empty list is returned`() {
        val response = getCall("/")
        assertEquals(HttpStatus.OK, response.status)
        assertEquals(emptyList<String>(), toList(response.body()))
    }

    @Test
    fun `short code is created`() {
        val response = postCall()
        assertEquals(HttpStatus.CREATED, response.status)
    }

    @Test
    fun `verify short code to url mapping`() {
        val response1 = postCall()
        assertEquals(HttpStatus.CREATED, response1.status)

        val response2 = getCall("/${response1.body()!!}")
        assertEquals(HttpStatus.MOVED_PERMANENTLY, response2.status)
        assertEquals("close", response2.headers[HttpHeaders.CONNECTION])
        assertEquals(randomUrl, response2.headers[HttpHeaders.LOCATION])
    }

    @Test
    fun `get all is working`() {
        val num = Random.nextInt(5, 10)
        repeat(num) {
            `short code is created`()
        }

        val response = getCall("/")
        val body = toList(response.body())
        assertEquals(HttpStatus.OK, response.status)
        assertEquals(num, body.size)

        body.forEach {
            assertEquals(randomUrl, it["url"].toString())
            assertEquals(0L, it["count"].toString().toLong())
        }
    }

    @Test
    fun `count is getting incremented`() {
        val shortCode = postCall().body()!!.toString()

        val num = Random.nextInt(5, 10)
        repeat(num) {
            getCall("/$shortCode")
        }

        val response = getCall("/")
        val singleElement = toList(response.body()).single()
        assertEquals(HttpStatus.OK, response.status)
        assertEquals(randomUrl, singleElement["url"])
        assertEquals(num, singleElement["count"])
    }

    @Test
    fun `invalid short code returns not found response`() {
        val randomCode = Random.nextInt()
        val exception = assertThrows<HttpClientResponseException> {
            getCall("/$randomCode")
        }
        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertTrue {
            exception.response.body()!!.toString().contains("Could not find mapping for short code: $randomCode")
        }
    }
}
