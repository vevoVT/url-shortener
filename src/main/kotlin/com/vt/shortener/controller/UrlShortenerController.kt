package com.vt.shortener.controller

import com.vt.shortener.model.UrlShortener
import com.vt.shortener.service.UrlShortenerService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.hateoas.JsonError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.net.URI
import kotlinx.coroutines.coroutineScope

@Controller("/url_shortener/v1")
class UrlShortenerController(
    private val urlShortenerService: UrlShortenerService
) {

    @Post("shorten")
    @Status(HttpStatus.CREATED)
    @Operation(summary = "Shortens a url", description = "Generates a short url from a given url")
    @ApiResponse(
        responseCode = "201",
        description = "Short url created",
        content = [Content(mediaType = "text/plain", schema = Schema(type = "string"))]
    )
    suspend fun shorten(@Body body: UrlShortener.Request): String = coroutineScope {
        urlShortenerService.shortenUrl(body.url)
    }

    @Get("{shortCode}")
    @Operation(summary = "Redirects via short url", description = "Redirects to the original url using short url")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "301",
            description = "Redirected to the original url"
        ), ApiResponse(
            responseCode = "404",
            description = "Short url does not exist",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = JsonError::class))]
        )]
    )
    suspend fun redirectUrl(@PathVariable shortCode: String): HttpResponse<String> = coroutineScope {
        val redirectUrl = urlShortenerService.fromShortCode(shortCode)
        HttpResponse.redirect(URI.create(redirectUrl))
    }

    @Get
    @Operation(
        summary = "Get all short urls",
        description = "Retrieves all short url, their mapping & the number of times they have been accessed"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Array of all short urls, url and their accessed count",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(
                type = "array",
                implementation = Array<UrlShortener.Response>::class,
                example = """[{ "url": "https://abcd.com", "short_code": "random", "count": 5 }]"""
            )
        )]
    )
    suspend fun getAll(): Array<UrlShortener.Response> = coroutineScope {
        urlShortenerService.getAll()
    }
}
