package com.gary.kotlin.chat

import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.dto.UserVM
import com.gary.kotlin.chat.extension.prepareForTesting
import com.gary.kotlin.chat.repository.ContentType
import com.gary.kotlin.chat.repository.Message
import com.gary.kotlin.chat.repository.MessageRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import java.net.URI
import java.net.URL
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.web.client.postForEntity
import java.time.temporal.ChronoUnit.MILLIS

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = [
		//"spring.datasource.url=jdbc:h2:mem:testdb"
		"spring.r2dbc.url=r2dbc:h2:mem:///testdb;USER=sa;PASSWORD=password"
	]
)
class ChatKotlinApplicationIT {
	//We use the lateinit keyword, which works perfectly for cases
	// where the initialization of non-null fields has to be deferred.
	@Autowired
	lateinit var client: TestRestTemplate

	@Autowired
	lateinit var messageRepository: MessageRepository

	lateinit var lastMessageId: String

	val now: Instant = Instant.now()

	@BeforeEach
	fun setUp() {
		runBlocking {
			val secondBeforeNow = now.minusSeconds(1)
			val twoSecondBeforeNow = now.minusSeconds(2)
			val savedMessages = messageRepository.saveAll(
				listOf(
					Message(
						"*testMessage*",
						ContentType.PLAIN,
						twoSecondBeforeNow,
						"test",
						"http://test.com"
					),
					Message(
						"**testMessage2**",
						ContentType.MARKDOWN,
						secondBeforeNow,
						"test1",
						"http://test.com"
					),
					Message(
						"`testMessage3`",
						ContentType.MARKDOWN,
						now,
						"test2",
						"http://test.com"
					)
				)
			)
			lastMessageId = savedMessages.first().id ?: ""
		}
	}

	@AfterEach
	fun tearDown() {
		runBlocking {
			messageRepository.deleteAll()
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = [true, false])
	fun `test that messages API returns latest messages`(withLastMessageId: Boolean) {
		runBlocking {
			val messages: List<MessageVM>? = client.exchange(
				RequestEntity<Any>(
					HttpMethod.GET,
					URI("/api/v1/messages?lastMessageId=${if (withLastMessageId) lastMessageId else ""}")
				),
				object : ParameterizedTypeReference<List<MessageVM>>() {}).body

			if (!withLastMessageId) {
				assertThat(messages?.map {
					it.prepareForTesting()
				})
					.first()
					.isEqualTo(
						MessageVM(
							"*testMessage*",
							UserVM("test", URL("http://test.com")),
							now.minusSeconds(2).truncatedTo(MILLIS)

						)
					)
			}

			assertThat(messages?.map { it.prepareForTesting() })
				.containsSubsequence(
					MessageVM(
						"<body><p><strong>testMessage2</strong></p></body>",
						UserVM("test1", URL("http://test.com")),
						now.minusSeconds(1).truncatedTo(MILLIS)
					),
					MessageVM(
						"<body><p><code>testMessage3</code></p></body>",
						UserVM("test2", URL("http://test.com")),
						now.truncatedTo(MILLIS)
					)
				)
		}
	}

	@Test
	fun `test that messages posted to the API is stored`() {
		runBlocking {
			client.postForEntity<Any>(
				URI("/api/v1/messages"),
				MessageVM(
					"`HelloWorld`",
					UserVM("test", URL("http://test.com")),
					now.plusSeconds(1)
				)
			)

			messageRepository.findAll()
				.first { it.content.contains("HelloWorld") }
				.apply {
					assertThat(this.prepareForTesting())
						.isEqualTo(
							Message(
								"`HelloWorld`",
								ContentType.MARKDOWN,
								now.plusSeconds(1).truncatedTo(MILLIS),
								"test",
								"http://test.com"
							)
						)
				}
		}
	}

}
