package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.dto.UserVM
import com.gary.kotlin.chat.repository.ContentType
import com.gary.kotlin.chat.repository.Message
import com.gary.kotlin.chat.repository.MessageRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.net.URL

@Service
@Primary
class PersistentMessageService(val messageRepository : MessageRepository) : MessageService {
    override fun latest(): List<MessageVM> =
        messageRepository.findLatest()
            .map { with(it){
                MessageVM(content, UserVM(username, URL(userAvatarImageLink)), sent, id)
            }
    }

    override fun after(lastMessageId: String): List<MessageVM> =
        messageRepository.findLatest(lastMessageId)
            .map { with(it){
                MessageVM(content, UserVM(username, URL(userAvatarImageLink)), sent, id)
            }
    }

    override fun post(message: MessageVM) {
        messageRepository.save(
            with(message) {
                Message(content, ContentType.PLAIN, sent, user.name, user.avatarImageLink.toString())
            }
        )
    }
}