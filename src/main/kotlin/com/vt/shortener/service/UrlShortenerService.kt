package com.vt.shortener.service

import com.vt.shortener.dao.DataStore
import com.vt.shortener.logging.Logging
import com.vt.shortener.logging.logger
import com.vt.shortener.model.UrlShortener
import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import javax.inject.Singleton

interface UrlShortenerService {
    suspend fun shortenUrl(url: String): String
    suspend fun fromShortCode(shortCode: String): String
    suspend fun getAll(): Array<UrlShortener.Response>
}

@Singleton
class DefaultUrlShortenerService(
    private val randomGeneratorService: RandomGeneratorService,
    private val dataStore: DataStore,
    private val meterRegistry: MeterRegistry
) : UrlShortenerService {

    override suspend fun shortenUrl(url: String): String {
        val shortCode = randomGeneratorService.randomString()
        return if (dataStore.get(shortCode) == null) {
            logger.info("Generated short code: {} for url: {}", shortCode, url)
            dataStore.post(shortCode, url)
            shortCode
        } else {
            shortenUrl(url)
        }
    }

    override suspend fun fromShortCode(shortCode: String): String =
        dataStore.get(shortCode)?.let {
            logger.info("For short code: {}, url: {}", shortCode, it.url)
            dataStore.update(it.shortCode, it.count + 1)
            incrementCounter(shortCode)
            it.url
        } ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Could not find mapping for short code: $shortCode")

    override suspend fun getAll(): Array<UrlShortener.Response> =
        dataStore.getAll().toTypedArray()

    private fun incrementCounter(shortCode: String) =
        meterRegistry.counter("short.code.accessed", "code", shortCode).increment()

    companion object : Logging {
        val logger = logger()
    }
}
