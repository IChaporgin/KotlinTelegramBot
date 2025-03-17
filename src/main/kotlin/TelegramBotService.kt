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
    val statisticsClicked = "statistics_clicked"
    val learnClicked = "learn_words_clicked"

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
                  "callback_data": "$learnClicked"
                }
              ],
              [
                {
                  "text": "Статистика",
                  "callback_data": "$statisticsClicked"
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

}

private const val TELEGRAM_URL = "https://api.telegram.org/bot"