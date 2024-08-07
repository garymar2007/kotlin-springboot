package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM

interface MessageService {

    fun latest(): List<MessageVM>

    fun after(messageId: String): List<MessageVM>

    fun post(message: MessageVM)
}