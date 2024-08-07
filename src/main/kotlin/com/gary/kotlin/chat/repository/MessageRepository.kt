package com.gary.kotlin.chat.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.query.Param

/**
 * Repository for messages: extends an ordinary CrudRepository and provides two different
 * methods with custom queries for retrieving the latest messages and for retrieving messages
 * associated with specific message IDs.
 */
interface MessageRepository : CrudRepository<Message, String> {
    // language=SQL
    // multiline String used to express the SQL query in the readable format.
    @Query("""
        SELECT * FROM (
            SELECT * FROM MESSAGES
            ORDER BY "SENT" DESC
            LIMIT 10
        ) ORDER BY "SENT"
    """)
    fun findLatest(): List<Message>

    // language=SQL
    @Query("""
        SELECT * FROM (
            SELECT * FROM MESSAGES
            WHERE SENT > (SELECT SENT FROM MESSAGES WHERE ID = :id)
            ORDER BY "SENT" DESC
        ) ORDER BY "SENT"
    """)
    fun findLatest(@Param("id") id: String): List<Message>
}