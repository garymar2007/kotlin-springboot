package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM

interface MessageService {

    suspend fun latest(): List<MessageVM>

    suspend fun after(messageId: String): List<MessageVM>

    suspend fun post(message: MessageVM)
}