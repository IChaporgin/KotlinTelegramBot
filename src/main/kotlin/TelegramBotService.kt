import kotlinx.serialization.json.Json
import org.example.LearnWordsTrainer
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(
    private val botToken: String,
) {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatId: Long?, message: String): String {
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString()) // Кодируем текст
        val url = "$TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedMessage"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()
        println("Response: $responseBody")

        return response.body()
    }

    fun sendMenu(json: Json, chatId: Long?): String {
        val url = "$TELEGRAM_URL$botToken/sendMessage"
        val sendMenuBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(InlineKeyboard(text = "Изучить слова", callbackData = LEARN_CLICKED)),
                    listOf(
                        InlineKeyboard(text = "Статистика", callbackData = STATISTICS_CLICKED)
                    )
                )
            )
        )

        val sendMenuRequest = json.encodeToString(sendMenuBody)

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuRequest, StandardCharsets.UTF_8))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        val responseBody = response.body()
        println("Response: $responseBody")

        return response.body()

    }

    fun sendQuestion(json: Json, chatId: Long?, words: LearnWordsTrainer.Question): String {
        val url = "$TELEGRAM_URL$botToken/sendMessage"
        val requestBody = SendMessageRequest(
            chatId = chatId,
            text = words.question.original,
            replyMarkup = ReplyMarkup(
                inlineKeyboard = words.answer.mapIndexed { index, word ->
                    listOf(InlineKeyboard(text = word.description, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"))
                }
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString, StandardCharsets.UTF_8))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

        return response.body()
    }
}

private const val TELEGRAM_URL = "https://api.telegram.org/bot"
const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"