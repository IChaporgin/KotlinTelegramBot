fun main(args: Array<String>) {
    val messages: MutableList<Message> = mutableListOf()
    val botService = TelegramBotService(args[0])
    var updateId = 0
    val updateIdText: String = "\"update_id\":(.+?),"

    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        println(updates)
        updateId = parsingWithRegex(updates, updateIdText)?.toIntOrNull()?.plus(1) ?: updateId
        if (!updates.contains("update_id")) continue
        messages.add(getData(updates))
        botService.sendMessage(
            getData(updates).messageId,
            getData(updates).message
        )
    }
}

fun getData(updates: String): Message {

    val messageText: String = "\"text\":\"(.+?)\""
    val message = parsingWithRegex(updates, messageText)?.let { decodeUnicodeString(it) } ?: "Нет сообщений"

    val firstNameText: String = "\"first_name\":\"(.+?)\""
    val firstName = parsingWithRegex(updates, firstNameText)

    val lastNameText: String = "\"last_name\":\"(.+?)\""
    val lastName = parsingWithRegex(updates, lastNameText)

    val messageIdText: String = "\"id\":(.+?),"
    val messageId = parsingWithRegex(updates, messageIdText)

    return Message(firstName.toString(), lastName.toString(), message, messageId.toString())
}

fun decodeUnicodeString(unicodeText: String): String {
    return unicodeText.replace("\\\\u([0-9A-Fa-f]{4})".toRegex()) {
        val charCode = it.groupValues[1].toInt(16)
        charCode.toChar().toString()
    }
}

fun parsingWithRegex(updates: String, text: String): String? {
    val textRegex = text.toRegex()
    val matchResult: MatchResult? = textRegex.find(updates)
    val groups = matchResult?.groups
    val textParsing = groups?.get(1)?.value
    return textParsing
}

data class Message(
    val firstName: String,
    val lastName: String,
    val message: String,
    val messageId: String,
)