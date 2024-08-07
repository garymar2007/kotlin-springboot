package com.gary.kotlin.chat.controller

import com.gary.kotlin.chat.service.MessageService
import com.gary.kotlin.chat.dto.MessageVM
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HtmlController(val messageService: MessageService) {

    @GetMapping("/")
    suspend fun index(model: Model): String {
        val messages: List<MessageVM> = messageService.latest()

        //overloading the set operator to set the model attributes
        model["messages"] = messages
        model["lastMessageId"] = messages.lastOrNull()?.id ?: ""
        //Null safety: ?. - safe call and ?: elvis operator

        return "chat"
    }
}