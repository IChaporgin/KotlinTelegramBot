import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {
    val messages: MutableList<TelegramBotService.Message> = mutableListOf()
    val botToken = args[0]
    var updateId = 0
    val updateIdRegex: Regex = "\"update_id\":(.+?),".toRegex()
    val botService = TelegramBotService()

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(botToken, updateId)
        println(updates)
        updateId = botService.parsingWithRegex(updates, updateIdRegex)?.toIntOrNull()?.plus(1) ?: updateId
        if (!updates.contains("update_id")) continue
        messages.add(botService.getData(updates))
        messages.forEach { println("${it.messageId} ${it.firstName} ${it.lastName} ${it.message}") }
//        botService.sendMessage(
//            botToken,
//            botService.getData(updates).messageId,
//            botService.getData(updates).message
//        )
    }
}