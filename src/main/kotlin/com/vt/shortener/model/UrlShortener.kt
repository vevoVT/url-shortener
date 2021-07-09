package com.vt.shortener.model

object UrlShortener {
    data class Request(val url: String)

    data class Response(
        val url: String,
        val shortCode: String,
        val count: Long
    )
}
