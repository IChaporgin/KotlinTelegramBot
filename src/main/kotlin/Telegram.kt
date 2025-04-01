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
    val trainers = HashMap<Long, LearnWordsTrainer>()
    val botService = TelegramBotService(args[0])
    var lastUpdatesId = 0L
    val json = Json {
        ignoreUnknownKeys = true
    }
    val currentQuestions = mutableMapOf<Long, LearnWordsTrainer.Question>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = botService.getUpdates(lastUpdatesId)
        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, botService, currentQuestions, trainers) }
        lastUpdatesId = sortedUpdates.last().updateId + 1
        val updates = response.result
        val firstUpdate = updates.firstOrNull() ?: continue
        val updateId = firstUpdate.updateId
        lastUpdatesId = updateId + 1
        println("Response: $response")
        println("Update: $updates")
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

fun handleUpdate(
    update: Update,
    json: Json,
    botService: TelegramBotService,
    currentQuestions: MutableMap<Long, LearnWordsTrainer.Question>,
    trainers: MutableMap<Long, LearnWordsTrainer>,
) {
    val message = update.message?.text
    val data = update.callbackQuery?.data
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val trainers = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }
    val question = trainers.question()


    if (message?.lowercase() == "/start") {
        botService.sendMenu(json, chatId)
    }

    if (data == LEARN_CLICKED) {
        currentQuestions[chatId] = question
        checkNextQuestionAndSend(trainers, botService, chatId, question)
    }

    if (data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true) {
        val question = currentQuestions[chatId] ?: return
        val answerIndex = data.removePrefix(CALLBACK_DATA_ANSWER_PREFIX).toInt()

        if (trainers.checkAnswer(answerIndex, question)) {
            val correctWord = trainers.dictionary.find { it.original == question.question.original }
            correctWord?.correctAnswersCount = (correctWord?.correctAnswersCount ?: 0) + 1
            trainers.saveDictionary(trainers.dictionary, "$chatId.txt")
            botService.sendMessage(chatId, "Верно!")
        } else {
            botService.sendMessage(
                chatId,
                "Не верно: ${question.question.original} это ${question.question.description}"
            )
        }

        val newQuestion = trainers.question()
        currentQuestions[chatId] = newQuestion // Сохраняем новый вопрос
        checkNextQuestionAndSend(trainers, botService, chatId, newQuestion)
    }
    if (data == STATISTICS_CLICKED) {
        botService.sendMessage(chatId, trainers.getStatistic(trainers.dictionary))
    }
    if (data == RESET_STATISTIC) {
        botService.sendMessage(chatId, "Статистика сброшена")
        trainers.dictionary.forEach { it.correctAnswersCount = 0 }
        trainers.saveDictionary(trainers.dictionary, "$chatId.txt")
    }
}
