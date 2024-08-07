package com.gary.kotlin.chat.extension;

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.repository.Message
import java.time.temporal.ChronoUnit.MILLIS

fun MessageVM.prepareForTesting() = copy(id = null, sent = sent.truncatedTo(MILLIS))

fun Message.prepareForTesting() = copy(id = null, sent = sent.truncatedTo(MILLIS))
