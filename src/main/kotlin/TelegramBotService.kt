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

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendMessage(chatID: String, message: String): String {
        val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString()) // Кодируем текст
        val url = "$TELEGRAM_URL$botToken/sendMessage?chat_id=$chatID&text=$encodedMessage"
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()
        println("Response: $responseBody")

        return response.body()
    }

    fun sendMenu(chatID: String): String {
        val url = "$TELEGRAM_URL$botToken/sendMessage"
        val sendMenuBody = """
        {
          "chat_id": "$chatID",
          "text": "Основное меню",
          "reply_markup": {
            "inline_keyboard": [
              [
                {
                  "text": "Изучить слова",
                  "callback_data": "$LEARN_CLICKED"
                }
              ],
              [
                {
                  "text": "Статистика",
                  "callback_data": "$STATISTICS_CLICKED"
                }
              ]
            ]
          }
        }
    """.trimIndent()

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody, StandardCharsets.UTF_8))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        val responseBody = response.body()
        println("Response: $responseBody")

        return response.body()

    }

    fun sendQuestion(chatID: String, words: LearnWordsTrainer.Question): String {
        val url = "$TELEGRAM_URL$botToken/sendMessage"
        val inlineKeyboard = words.answer
            .mapIndexed { index, word ->
                """
        [
          {
            "text": "${index + 1} - ${word.description}",
            "callback_data": "$CALLBACK_DATA_ANSWER_PREFIX${index + 1}"
          }
        ]
        """
            }.joinToString(",")
        val sendQuestionBody = """
        {
          "chat_id": "$chatID",
          "text": "${words.question.original}",
          "reply_markup": {
            "inline_keyboard": [
            $inlineKeyboard
            ]
            }
        }
    """

        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(sendQuestionBody, StandardCharsets.UTF_8))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        val responseBody = response.body()
        println("Response: $responseBody")

        return response.body()
    }
}

private const val TELEGRAM_URL = "https://api.telegram.org/bot"
const val STATISTICS_CLICKED = "statistics_clicked"
const val LEARN_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"