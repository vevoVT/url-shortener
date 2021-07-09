package com.vt.shortener.dao

import com.vt.shortener.model.UrlShortener

interface DataStore {
    suspend fun get(shortCode: String): UrlShortener.Response?
    suspend fun getAll(): List<UrlShortener.Response>
    suspend fun post(shortCode: String, url: String)
    suspend fun update(shortCode: String, count: Long)
}
