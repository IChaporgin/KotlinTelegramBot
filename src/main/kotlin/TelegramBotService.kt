import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService {

    data class Message (
        val firstName: String,
        val lastName: String,
        val message: String,
        val messageId: String,
    )

    fun getData(updates: String) : Message {

        val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
        val message = parsingWithRegex(updates, messageTextRegex)?.let { decodeUnicodeString(it) } ?: "Нет сообщений"

        val firstNameRegex: Regex = "\"first_name\":\"(.+?)\"".toRegex()
        val firstName = parsingWithRegex(updates, firstNameRegex)

        val lastNameRegex: Regex = "\"last_name\":\"(.+?)\"".toRegex()
        val lastName = parsingWithRegex(updates, lastNameRegex)

        val messageIdRegex: Regex = "\"id\":(.+?),".toRegex()
        val messageId = parsingWithRegex(updates, messageIdRegex)

        return Message(firstName.toString(), lastName.toString(), message, messageId.toString())
    }

    fun getUpdates(botToken: String, updateId: Int) : String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val response= client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun parsingWithRegex(updates: String, textRegex: Regex) : String? {
        val matchResult: MatchResult? = textRegex.find(updates)
        val groups = matchResult?.groups
        val text = groups?.get(1)?.value
        return text
    }

    fun sendMessage(botToken: String, chatID: String, message: String) : String {
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatID&text=$message"
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response= client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    private fun decodeUnicodeString(unicodeText: String): String {
        return unicodeText.replace("\\\\u([0-9A-Fa-f]{4})".toRegex()) {
            val charCode = it.groupValues[1].toInt(16)
            charCode.toChar().toString()
        }
    }
}