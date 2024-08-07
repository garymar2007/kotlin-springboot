package com.gary.kotlin.chat.extension

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.dto.UserVM
import com.gary.kotlin.chat.repository.ContentType
import com.gary.kotlin.chat.repository.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URL
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

fun MessageVM.asDomainObject(contentType: ContentType = ContentType.MARKDOWN) : Message = Message(
    content,
    contentType,
    sent,
    user.name,
    user.avatarImageLink.toString(),
    id
)

fun Message.asViewModel(): MessageVM = MessageVM(
    contentType.render(content),
    UserVM(username, URL(userAvatarImageLink)),
    sent,
    id
)

fun Flow<Message>.mapToViewModel(): Flow<MessageVM> = map { it.asViewModel() }

fun ContentType.render(content: String): String = when(this) {
    ContentType.PLAIN -> content
    ContentType.MARKDOWN -> {
        val flavour = CommonMarkFlavourDescriptor()
        HtmlGenerator(content, MarkdownParser(flavour).buildMarkdownTreeFromString(content),
            flavour).generateHtml()
    }
}

fun MessageVM.asRendered(contentType: ContentType = ContentType.MARKDOWN): MessageVM =
    this.copy(content = contentType.render(this.content))