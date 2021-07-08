package com.vt.shortener

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("com.vt.shortener")
		.start()
}

