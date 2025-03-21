import org.example.COUNT_ANSWER
import org.example.LearnWordsTrainer

fun main(args: Array<String>) {
    val messages: MutableList<Message> = mutableListOf()
    val botService = TelegramBotService(args[0])
    var updateId = 0
    val updateIdText: String = "\"update_id\":(.+?),"
    val dataRegex = "\"data\":\"(.+?)\"}}]"
    val trainer = LearnWordsTrainer()
    while (true) {
        Thread.sleep(2000)
        val updates: String = botService.getUpdates(updateId)
        val data = parsingWithRegex(updates, dataRegex)
        updateId = parsingWithRegex(updates, updateIdText)?.toIntOrNull()?.plus(1) ?: updateId
        if (!updates.contains("update_id")) continue
        println(updates)
        if (decodeUnicodeString(getData(updates).message) == "/start") {
            botService.sendMenu(getData(updates).chatID)
        } else {
            if (updates.contains("callback_data")) {
                val questions = trainer.question(trainer.dictionary)
                when {
                    data == LEARN_CLICKED -> checkNextQuestionAndSend(
                        trainer, botService, getData(updates).chatID.toInt()
                    )

                    data == STATISTICS_CLICKED -> botService.sendMessage(
                        getData(updates).chatID, trainer.getStatistic(trainer.dictionary)
                    )

                    data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                        val answerIndex = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX)
                        if (trainer.checkAnswer(answerIndex.toInt(), questions)) {
                            botService.sendMessage(getData(updates).chatID, "Верно!")
                            checkNextQuestionAndSend(trainer, botService, getData(updates).chatID.toInt())
                            println("Индекс = $answerIndex")
                        } else {
                            botService.sendMessage(
                                getData(updates).chatID,
                                "Не верно: ${questions.question.original} это ${questions.question.description}"
                            )
                            println("Индекс при втором варианте = $answerIndex")
                            checkNextQuestionAndSend(trainer, botService, getData(updates).chatID.toInt())
                        }
                    }
                }
                continue
            } else {
                messages.add(getData(updates))
                botService.sendMessage(
                    getData(updates).chatID, getData(updates).message
                )
            }
        }
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

    val chatIDRegex: Regex = "\"chat\":\\{\"id\":(\\d+)".toRegex()
    val chatId = parsingWithRegex(updates, chatIDRegex.toString())

    return Message(firstName.toString(), lastName.toString(), message, messageId.toString(), chatId.toString())
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

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Int,
) {
    val dictionary = trainer.dictionary
    val notLearnedWords = dictionary.filter { it.correctAnswersCount < COUNT_ANSWER }

    if (notLearnedWords.isEmpty()) {
        telegramBotService.sendMessage(chatId.toString(), "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(chatId.toString(), trainer.question(dictionary))
    }
}

data class Message(
    val firstName: String,
    val lastName: String,
    val message: String,
    val messageId: String,
    val chatID: String,
)