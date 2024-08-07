package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.dto.UserVM
import com.gary.kotlin.chat.extension.asDomainObject
import com.gary.kotlin.chat.extension.asViewModel
import com.gary.kotlin.chat.extension.mapToViewModel
import com.gary.kotlin.chat.repository.ContentType
import com.gary.kotlin.chat.repository.Message
import com.gary.kotlin.chat.repository.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.net.URL

//@Primary to give higher preference to a bean when there are multiple beans of the same type.
@Service
@Primary
class PersistentMessageService(val messageRepository : MessageRepository) : MessageService {
    override suspend fun latest(): List<MessageVM> =
        messageRepository.findLatest().mapToViewModel()

    override suspend fun after(lastMessageId: String): List<MessageVM> =
        messageRepository.findLatest(lastMessageId).mapToViewModel()

    override suspend fun post(message: MessageVM) {
        messageRepository.save(message.asDomainObject())
    }
}