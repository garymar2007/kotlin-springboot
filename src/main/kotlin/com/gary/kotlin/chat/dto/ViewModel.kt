package com.gary.kotlin.chat.dto

import java.net.URL
import java.time.Instant

//data classes are classes that are meant to hold data. They automatically implement
// equals(), hashCode(), toString() and copy() functions.
data class MessageVM(val content: String, val user: UserVM, val sent: Instant, val id: String? = null)

data class UserVM(val name: String, val avatarImageLink: URL)