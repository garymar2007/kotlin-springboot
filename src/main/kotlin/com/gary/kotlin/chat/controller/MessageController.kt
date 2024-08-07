package com.gary.kotlin.chat.controller

import com.gary.kotlin.chat.service.MessageService
import com.gary.kotlin.chat.dto.MessageVM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onStart
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.*

@RestController
@MessageMapping("api.v1.messages")
class MessageController(val messageService: MessageService) {

    @MessageMapping("stream")
    suspend fun receive(@Payload inboundMessages: Flow<MessageVM>) =
        messageService.post(inboundMessages)

    @MessageMapping("stream")
    suspend fun send(): Flow<MessageVM> = messageService
        .stream()
        .onStart {
            emitAll(messageService.latest())
        }

//    @GetMapping
//    suspend fun latest(@RequestParam(value = "lastMessageId", defaultValue = "") lastMessageId: String):
//            ResponseEntity<Flow<MessageVM>> {
//        val messages = if (lastMessageId.isNotEmpty()) {
//            messageService.after(lastMessageId)
//        } else {
//            messageService.latest()
//        }
//
//        //Scope function: with whose purpose is to execute a block of code within the context of an object.
//        return if (messages.isEmpty()) {
//            with(ResponseEntity.noContent()) {
//                header("lastMessageId", lastMessageId)
//                build<Flow<MessageVM>>()
//            }
//        } else {
//            with(ResponseEntity.ok()) {
//                header("lastMessageId", messages.last().id)
//                body(messages)
//            }
//        }
//    }
//
//    @PostMapping
//    suspend fun post(@RequestBody message: MessageVM) {
//        messageService.post(message)
//    }
}
