package com.gary.kotlin.chat.controller

import com.gary.kotlin.chat.service.MessageService
import com.gary.kotlin.chat.dto.MessageVM
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/messages")
class MessageController(val messageService: MessageService) {

    @GetMapping
    suspend fun latest(@RequestParam(value = "lastMessageId", defaultValue = "") lastMessageId: String): ResponseEntity<List<MessageVM>> {
        val messages = if (lastMessageId.isNotEmpty()) {
            messageService.after(lastMessageId)
        } else {
            messageService.latest()
        }

        //Scope function: with whose purpose is to execute a block of code within the context of an object.
        return if (messages.isEmpty()) {
            with(ResponseEntity.noContent()) {
                header("lastMessageId", lastMessageId)
                build<List<MessageVM>>()
            }
        } else {
            with(ResponseEntity.ok()) {
                header("lastMessageId", messages.last().id)
                body(messages)
            }
        }
    }

    @PostMapping
    suspend fun post(@RequestBody message: MessageVM) {
        messageService.post(message)
    }
}
