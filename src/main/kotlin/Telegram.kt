import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val messages: MutableList<Message> = mutableListOf()
    val botToken = args[0]
    var updateId = 0
    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()

    while (true){
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)
        updateId = parsingWithRegex(updates, updateIdRegex)?.toIntOrNull()?.plus(1) ?: updateId
        if (!updates.contains("update_id")) continue
        messages.add(getData(updates))
        messages.forEach { println("${it.firstName}, ${it.lastName}, ${it.message}") }
        // Знаю, что тупо так распечатывать в цикле, но это просто для проверки работоспособности :)
    }
}

fun getData(updates: String) : Message {

    val messageTextRegex: Regex = "\"text\":\"(.+?)\"".toRegex()
    val text = parsingWithRegex(updates, messageTextRegex)

    val firstNameRegex: Regex = "\"first_name\":\"(.+?)\"".toRegex()
    val firstName = parsingWithRegex(updates, firstNameRegex)

    val lastNameRegex: Regex = "\"last_name\":\"(.+?)\"".toRegex()
    val lastName = parsingWithRegex(updates, lastNameRegex)

    return Message(firstName.toString(), lastName.toString(), text.toString())
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

data class Message (
    val firstName: String,
    val lastName: String,
    val message: String,
)
