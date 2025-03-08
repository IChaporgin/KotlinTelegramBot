import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val messages: MutableList<Message> = mutableListOf()
    val botToken = args[0]
    var updateId = 0

    while (true){
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)
        println(updates)

        val startUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")
        if (startUpdateId == -1 || endUpdateId == -1) continue
        val updateIdString = updates.substring(startUpdateId + 11, endUpdateId)
        updateId = updateIdString.toInt() + 1

        messages.add(getData(updates))
        messages.forEach { println("${it.firstName}, ${it.lastName}, ${it.message}") }
        // Знаю, что тупо так распечатывать в цикле, но это просто для проверки работоспособности :)
    }
}

fun getData(updates: String) : Message {
    val startMessage = updates.lastIndexOf("text")
    val endMessage = updates.lastIndexOf("\"}}]}")
    val message = updates.substring(startMessage + 7, endMessage)

    val startFirstName = updates.lastIndexOf("first_name")
    val endFirstName = updates.lastIndexOf("\",\"last_name")
    val firstNameString = updates.substring(startFirstName + 13, endFirstName)

    val startLastName = updates.lastIndexOf("last_name")
    val endLastName = updates.lastIndexOf("\",\"username")
    val lastNameString = updates.substring(startLastName + 12, endLastName)

    return Message(firstNameString, lastNameString, message)
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val request: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response= client.send(request, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

data class Message (
    val firstName: String,
    val lastName: String,
    val message: String,
)
