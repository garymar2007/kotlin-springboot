package com.gary.kotlin.chat.service

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.dto.UserVM
import com.github.javafaker.Faker
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant
import kotlin.random.Random

@Service
class FakeMessageService : MessageService {

    val users: Map<String, UserVM> = mapOf(
        "Shakespeare"  to UserVM("Shakespeare", URL("https://blog.12min.com/wp-content/uploads/2018/05/27d-William-Shakespeare.jpg")),
        "RickAndMorty" to UserVM("RickAndMorty", URL("http://thecircular.org/wp-content/uploads/2015/04/rick-and-morty-fb-pic1.jpg")),
        "Yoda"         to UserVM("Yoda", URL("https://news.toyark.com/wp-content/uploads/sites/4/2019/03/SH-Figuarts-Yoda-001.jpg"))
    )

    //functional type which is used in a form of lambda expression where takes no arguments and returns a string.
    val usersQuotes: Map<String, () -> String> = mapOf(
        "Shakespeare"  to { Faker.instance().shakespeare().asYouLikeItQuote() },
        "RickAndMorty" to { Faker.instance().rickAndMorty().quote() },
        "Yoda"         to { Faker.instance().yoda().quote() }
    )

    override fun latest(): List<MessageVM> {
        val count = Random.nextInt(1, 15)
        return (0..count).map {
            // use the extension method random() on the values of the map to get a random user
            val user = users.values.random()
            val userQuote = usersQuotes.getValue(user.name).invoke()

            MessageVM(userQuote, user, Instant.now(), Random.nextBytes(10).toString())
        }.toList()
    }

    override fun after(messageId: String): List<MessageVM> {
        return latest()
    }

    override fun post(message: MessageVM) {
        TODO("Not yet implemented")
    }
}
