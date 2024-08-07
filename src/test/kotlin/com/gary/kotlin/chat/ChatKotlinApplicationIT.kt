package com.gary.kotlin.chat

import app.cash.turbine.test
import com.gary.kotlin.chat.dto.MessageVM
import com.gary.kotlin.chat.dto.UserVM
import com.gary.kotlin.chat.extension.prepareForTesting
import com.gary.kotlin.chat.repository.ContentType
import com.gary.kotlin.chat.repository.Message
import com.gary.kotlin.chat.repository.MessageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.net.URI
import java.net.URL
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import java.time.temporal.ChronoUnit.MILLIS
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	properties = [
		//"spring.datasource.url=jdbc:h2:mem:testdb"
		"spring.r2dbc.url=r2dbc:h2:mem:///testdb;USER=sa;PASSWORD=password"
	]
)
class ChatKotlinApplicationIT (@Autowired val rsocketBuilder: RSocketRequester.Builder,
							   @Autowired val messageRepository: MessageRepository,
							   @LocalServerPort val serverPort: Int) {
	//We use the lateinit keyword, which works perfectly for cases
	// where the initialization of non-null fields has to be deferred.
//	@Autowired
//	lateinit var client: TestRestTemplate
//
//	@Autowired
//	lateinit var messageRepository: MessageRepository

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

	@ExperimentalTime
	@ExperimentalCoroutinesApi
	@Test
	fun `test that messages API returns latest messages`() {
		runBlocking {
			val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

			rSocketRequester
				.route("api.v1.messages.stream")
				.retrieveFlow<MessageVM>()
				.test {
					assertThat(expectItem().prepareForTesting())
						.isEqualTo(
							MessageVM(
								"*testMessage*",
								UserVM("test", URL("http://test.com")),
								now.minusSeconds(2).truncatedTo(MILLIS)
							)
						)
					assertThat(expectItem().prepareForTesting())
						.isEqualTo(
							MessageVM(
								"<body><p><strong>testMessage2</strong></p></body>",
								UserVM("test1", URL("http://test.com")),
								now.minusSeconds(1).truncatedTo(MILLIS)
							)
						)
					assertThat(expectItem().prepareForTesting())
						.isEqualTo(
							MessageVM(
								"<body><p><code>testMessage3</code></p></body>",
								UserVM("test2", URL("http://test.com")),
								now.truncatedTo(MILLIS)
							)
						)

					expectNoEvents()

					launch {
						rSocketRequester.route("api.v1.messages.stream")
							.dataWithType(flow {
								emit(
									MessageVM(
										"`HelloWorld`",
										UserVM("test", URL("http://test.com")),
										now.plusSeconds(1)
									)
								)
							})
							.retrieveFlow<Void>()
							.collect()
					}

					assertThat(expectItem().prepareForTesting())
						.isEqualTo(
							MessageVM(
								"<body><p><code>HelloWorld</code></p></body>",
								UserVM("test", URL("http://test.com")),
								now.plusSeconds(1).truncatedTo(MILLIS)
							)
						)
					cancelAndIgnoreRemainingEvents()
				}
		}

	}

	@ExperimentalTime
	@Test
	fun `test that messages streamed to the API is stored`() {
		runBlocking {
			launch {
				val rSocketRequester =
					rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

				rSocketRequester.route("api.v1.messages.stream")
					.dataWithType(flow {
						emit(
							MessageVM(
								"`HelloWorld`",
								UserVM("test", URL("http://test.com")),
								now.plusSeconds(1)
							)
						)
					})
					.retrieveFlow<Void>()
					.collect()
			}

			delay(2.seconds)

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

	//@ParameterizedTest
	//@ValueSource(booleans = [true, false])
//	fun `test that messages API returns latest messages`(withLastMessageId: Boolean) {
//		runBlocking {
//			val messages: List<MessageVM>? = client.exchange(
//				RequestEntity<Any>(
//					HttpMethod.GET,
//					URI("/api/v1/messages?lastMessageId=${if (withLastMessageId) lastMessageId else ""}")
//				),
//				object : ParameterizedTypeReference<List<MessageVM>>() {}).body
//
//			if (!withLastMessageId) {
//				assertThat(messages?.map {
//					it.prepareForTesting()
//				})
//					.first()
//					.isEqualTo(
//						MessageVM(
//							"*testMessage*",
//							UserVM("test", URL("http://test.com")),
//							now.minusSeconds(2).truncatedTo(MILLIS)
//
//						)
//					)
//			}
//
//			assertThat(messages?.map { it.prepareForTesting() })
//				.containsSubsequence(
//					MessageVM(
//						"<body><p><strong>testMessage2</strong></p></body>",
//						UserVM("test1", URL("http://test.com")),
//						now.minusSeconds(1).truncatedTo(MILLIS)
//					),
//					MessageVM(
//						"<body><p><code>testMessage3</code></p></body>",
//						UserVM("test2", URL("http://test.com")),
//						now.truncatedTo(MILLIS)
//					)
//				)
//		}
//	}

//	@Test
//	fun `test that messages posted to the API is stored`() {
//		runBlocking {
//			client.postForEntity<Any>(
//				URI("/api/v1/messages"),
//				MessageVM(
//					"`HelloWorld`",
//					UserVM("test", URL("http://test.com")),
//					now.plusSeconds(1)
//				)
//			)
//
//			messageRepository.findAll()
//				.first { it.content.contains("HelloWorld") }
//				.apply {
//					assertThat(this.prepareForTesting())
//						.isEqualTo(
//							Message(
//								"`HelloWorld`",
//								ContentType.MARKDOWN,
//								now.plusSeconds(1).truncatedTo(MILLIS),
//								"test",
//								"http://test.com"
//							)
//						)
//				}
//		}
//	}
//
//}
