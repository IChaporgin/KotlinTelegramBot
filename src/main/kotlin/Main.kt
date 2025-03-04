package org.example
import java.io.File

fun main() {

    fun getStatistic(data: List<Words>) : String {
        val totalCount = data.count()
        val learnedCount = data.filter { it.correctAnswersCount >= COUNT_ANSWER }
            .count()
        val percent = (learnedCount.toDouble() / totalCount.toDouble() * 100.00).toInt()
        return "Выучено $learnedCount из $totalCount слов | $percent %"
    }

    val dictionary = loadDictionary()

    while (true) {
        println(
            """
            Меню:
            1 - Учить слова
            2 - Статистика
            0 - Выход
        """.trimIndent()
        )

        val input = readlnOrNull()?.toIntOrNull()

        when (input) {
            1 -> {
                println("Вы выбрали учить слова")
                guessingWords(dictionary)
            }
            2 -> {
                println(getStatistic(dictionary))
            }
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

data class Words(
    val original: String,
    val description: String,
    var correctAnswersCount: Int = 0
)

fun loadDictionary() : List<Words> {
    val list: MutableList<Words> = mutableListOf()
    val wordsFile: File = File("words.txt")
    wordsFile.forEachLine { line ->
        val words = line.split('|')
        val original = words[0]
        val description = words[1]
        val countAnswer: Int = words.getOrNull(2)?.toIntOrNull() ?: 0
        list.add(Words(original, description, countAnswer))
    }
    return list
}

fun guessingWords(words: List<Words>) {
    while (true) {
        if (words.isEmpty()) {
            println("Все слова в словаре выучены")
            return
        } else {
            val questionWords = words.filter { it.correctAnswersCount < COUNT_ANSWER }
                .shuffled()
                .take(COUNT_QUESTION)
            val correctAnswer = questionWords.random()
            val correctAnswerId = questionWords.indexOf(correctAnswer)
            println("${correctAnswer.original}:")
            questionWords.shuffled()
            questionWords.forEachIndexed { index, word ->
                println("${index + 1} - ${word.description}")
            }
            println("----------\n0 - Меню")
            val userAnswerInput = readlnOrNull()?.toIntOrNull() ?: 0
            if (userAnswerInput == 0) break
            if (userAnswerInput == correctAnswerId + 1) {
                println("Правильно!")
                words.find { it.original == correctAnswer.original }?.correctAnswersCount = correctAnswer.correctAnswersCount + 1
                saveDictionary(words)
            } else {
                println("Не правильно! ${correctAnswer.original} - это ${correctAnswer.description}")
            }
        }
    }
}

fun saveDictionary(dictionary: List<Words>) {
    val file = File("words.txt")
    file.writeText(dictionary.joinToString("\n") {"${it.original}|${it.description}|${it.correctAnswersCount}"})
}

const val COUNT_ANSWER = 3
const val COUNT_QUESTION = 4