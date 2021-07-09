package com.vt.shortener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import javax.inject.Singleton

@Singleton
class ObjectMapperConfigurer : BeanCreatedEventListener<ObjectMapper> {
    override fun onCreated(event: BeanCreatedEvent<ObjectMapper>): ObjectMapper =
        event.bean.let { mapper ->
            mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
            mapper
        }
}
