package org.example

import java.io.File

data class Words(
    val original: String,
    val description: String,
    var correctAnswersCount: Int = 0
)

class LearnWordsTrainer {
    val dictionary = loadDictionary()
    private val countAnswer = 3
    private val countQuestion = 4

    private fun loadDictionary(): List<Words> {
        val list: MutableList<Words> = mutableListOf()
        val wordsFile = File("words.txt")
        wordsFile.forEachLine { line ->
            val words = line.split('|')
            val original = words[0]
            val description = words[1]
            val countAnswer: Int = words.getOrNull(2)?.toIntOrNull() ?: 0
            list.add(Words(original, description, countAnswer))
        }
        return list
    }

    private fun saveDictionary(dictionary: List<Words>) {
        val file = File("words.txt")
        file.writeText(dictionary.joinToString("\n") { "${it.original}|${it.description}|${it.correctAnswersCount}" })
    }

    fun guessingWords(words: List<Words>) {
        val countNotLearnedWords = words.count { it.correctAnswersCount < countAnswer }

        while (true) {
            val notLearningWords = words.filter { it.correctAnswersCount < countAnswer }
            if (notLearningWords.isEmpty()) {
                println("Все слова в словаре выучены")
                return
            }
            val questionWords = if (notLearningWords.count() > countAnswer) {
                words.filter { it.correctAnswersCount < countAnswer }
                    .shuffled()
                    .take(countQuestion)
            } else {
                (notLearningWords + words.filter { it.correctAnswersCount > countAnswer }
                    .shuffled()
                    .take(countQuestion - countNotLearnedWords)
                ).shuffled()
            }

            val correctAnswer = questionWords.random()

            val correctAnswerId = questionWords.indexOf(correctAnswer)
            println("${correctAnswer.original}:")
//            questionWords.shuffled()
            questionWords.forEachIndexed { index, word ->
                println("${index + 1} - ${word.description}")
            }
            println("----------\n0 - Меню")
            val userAnswerInput = readlnOrNull()?.toIntOrNull() ?: 0
            if (userAnswerInput == 0) break
            if (userAnswerInput == correctAnswerId + 1) {
                println("Правильно!")
                words.find { it.original == correctAnswer.original }?.correctAnswersCount =
                    correctAnswer.correctAnswersCount + 1
                saveDictionary(words)
            } else {
                println("Не правильно! ${correctAnswer.original} - это ${correctAnswer.description}")
            }
        }
    }

    fun getStatistic(data: List<Words>): String {
        val totalCount = data.count()
        val learnedCount = data.filter { it.correctAnswersCount >= countAnswer }
            .count()
        val percent = (learnedCount.toDouble() / totalCount.toDouble() * 100.00).toInt()
        return "Выучено $learnedCount из $totalCount слов | $percent %"
    }
}