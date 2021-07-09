package com.vt.shortener.dao

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.cql.Row
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException
import com.vt.shortener.logging.Logging
import com.vt.shortener.logging.logger
import com.vt.shortener.model.UrlShortener
import io.micronaut.context.annotation.Value
import io.micronaut.http.server.exceptions.InternalServerException
import javax.inject.Singleton

@Singleton
class CassandraStore(
    private val cqlSession: CqlSession,
    @Value("\${cassandra.table}") private val table: String
) : DataStore {

    private val createTableQuery: String =
        "CREATE TABLE $table (short_code text, url text, count bigint, PRIMARY KEY(short_code))"

    private lateinit var insertPreparedStatement: PreparedStatement
    private lateinit var updatePreparedStatement: PreparedStatement
    private lateinit var selectOnePreparedStatement: PreparedStatement
    private lateinit var selectAllPreparedStatement: PreparedStatement

    init {
        createTable()
        createPreparedStatements()
    }

    private fun createTable() =
        try {
            cqlSession.execute(createTableQuery)
            logger.info("Table: {} created in cassandra", table)
        } catch (e: AlreadyExistsException) {
            logger.info("Using existing table: {} in cassandra", table)
        } catch (e: Exception) {
            logger.error("Create table failed!", e)
            throw InternalServerException("Error connecting to cassandra", e)
        }

    private fun createPreparedStatements() {
        insertPreparedStatement = prepareStatement("INSERT INTO $table (short_code, url, count) VALUES (?, ?, ?)")
        selectOnePreparedStatement = prepareStatement("SELECT short_code, url, count FROM $table WHERE short_code = ?")
        selectAllPreparedStatement = prepareStatement("SELECT short_code, url, count FROM $table LIMIT ?")
        updatePreparedStatement = prepareStatement("UPDATE $table SET count = ? WHERE short_code = ?")
    }

    private fun prepareStatement(query: String) =
        cqlSession.prepare(SimpleStatement.newInstance(query))

    private fun Row.toResponseObject(): UrlShortener.Response? = try {
        UrlShortener.Response(
            shortCode = get(0, String::class.java)!!,
            url = get(1, String::class.java)!!,
            count = get(2, Long::class.java)!!
        )
    } catch (e: NullPointerException) {
        logger.error("Error while mapping row: $this")
        null
    }

    override suspend fun get(shortCode: String): UrlShortener.Response? {
        val resultSet = cqlSession.execute(selectOnePreparedStatement.bind(shortCode))
        return resultSet.firstOrNull()?.toResponseObject()
    }

    override suspend fun getAll(): List<UrlShortener.Response> {
        val resultSet = cqlSession.execute(selectAllPreparedStatement.bind(50))
        return resultSet.mapNotNull { it.toResponseObject() }
    }

    override suspend fun post(shortCode: String, url: String) {
        cqlSession.execute(insertPreparedStatement.bind(shortCode, url, 0L))
    }

    override suspend fun update(shortCode: String, count: Long) {
        cqlSession.execute(updatePreparedStatement.bind(count, shortCode))
    }

    companion object : Logging {
        val logger = logger()
    }
}
