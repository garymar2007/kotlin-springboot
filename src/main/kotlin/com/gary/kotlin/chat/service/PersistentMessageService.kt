package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.extension.asDomainObject
import com.gary.kotlin.chat.extension.asRendered
import com.gary.kotlin.chat.extension.mapToViewModel
import com.gary.kotlin.chat.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.collect
import org.springframework.stereotype.Service

//@Primary to give higher preference to a bean when there are multiple beans of the same type.
@Service
class PersistentMessageService(val messageRepository : MessageRepository) : MessageService {
    val sender: MutableSharedFlow<MessageVM> = MutableSharedFlow()

    override suspend fun latest(): Flow<MessageVM> =
        messageRepository.findLatest().mapToViewModel()

    override suspend fun after(lastMessageId: String): Flow<MessageVM> =
        messageRepository.findLatest(lastMessageId).mapToViewModel()

    override suspend fun stream(): Flow<MessageVM> = sender

    override suspend fun post(messages: Flow<MessageVM>) =
        messages
            .onEach { sender.emit(it.asRendered()) }
            .map { it.asDomainObject() }
            .let { messageRepository.saveAll(it) }
            .collect()
}