import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.example.COUNT_ANSWER
import org.example.LearnWordsTrainer

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,
    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,
    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long,
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>
)

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,
    @SerialName("callback_data")
    val callbackData: String,
)

fun main(args: Array<String>) {

    val botService = TelegramBotService(args[0])
    var lastUpdatesId = 0L
    val trainer = LearnWordsTrainer()
    val currentQuestions = mutableMapOf<Long, LearnWordsTrainer.Question>()
    val json = Json {
        ignoreUnknownKeys = true
    }

    while (true) {
        Thread.sleep(2000)
        val responseString: String = botService.getUpdates(lastUpdatesId)
        val response: Response = json.decodeFromString(responseString)
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdatesId = updateId + 1
        val message = firstUpdate.message?.text
        val data = firstUpdate.callbackQuery?.data
        val chatId = firstUpdate.message?.chat?.id ?: firstUpdate.callbackQuery?.message?.chat?.id ?: 0

        println(response)

        if (message?.lowercase() == "/start") {
            botService.sendMenu(json, chatId)
        } else {
            println(data)
            when {
                data == LEARN_CLICKED -> {
                    val question = trainer.question()
                    currentQuestions[chatId] = question
                    checkNextQuestionAndSend(trainer, botService, chatId, question)
                }

                data == STATISTICS_CLICKED -> botService.sendMessage(
                    chatId, trainer.getStatistic(trainer.dictionary)
                )

                data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true -> {
                    val question = currentQuestions[chatId] ?: return
                    val answerIndex = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX)
                    if (trainer.checkAnswer(answerIndex.toInt(), question)) {
                        botService.sendMessage(chatId, "Верно!")
                    } else {
                        botService.sendMessage(
                            chatId,
                            "Не верно: ${question.question.original} это ${question.question.description}"
                        )
                    }
                    val newQuestion = trainer.question()
                    currentQuestions[chatId] = newQuestion
                    checkNextQuestionAndSend(trainer, botService, chatId, newQuestion)
                }
            }
            continue
        }
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegramBotService: TelegramBotService,
    chatId: Long?,
    question: LearnWordsTrainer.Question
) {
    val dictionary = trainer.dictionary
    val notLearnedWords = dictionary.filter { it.correctAnswersCount < COUNT_ANSWER }

    if (notLearnedWords.isEmpty()) {
        telegramBotService.sendMessage(chatId, "Все слова в словаре выучены")
    } else {
        telegramBotService.sendQuestion(Json, chatId, question)
    }
}
