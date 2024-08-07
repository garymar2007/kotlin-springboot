package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM
import kotlinx.coroutines.flow.Flow

interface MessageService {

    suspend fun latest(): Flow<MessageVM>

    suspend fun after(messageId: String): Flow<MessageVM>

    suspend fun stream(): Flow<MessageVM>

    suspend fun post(message: Flow<MessageVM>)
}