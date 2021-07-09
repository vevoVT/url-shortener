package com.vt.shortener.service

import io.micronaut.context.annotation.Value
import javax.inject.Singleton
import kotlin.random.Random

interface RandomGeneratorService {
    fun randomString(): String
}

@Singleton
class DefaultRandomGeneratorService(
    @Value("\${random-string.max-limit}") private val maxLimit: Int
) : RandomGeneratorService {

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    override fun randomString(): String = (1..maxLimit.randomSize())
        .map { Random.nextInt(from = 0, until = charPool.size) }
        .map(charPool::get)
        .joinToString("")

    private fun Int.randomSize(): Int =
        Random.nextInt(from = this / 2, until = this) + 1
}
