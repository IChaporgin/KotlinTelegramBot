package org.example

import java.io.File

class LearnWordsTrainer(
    private val fileName: String = "words.txt",
) {

    data class Words(
        val original: String,
        val description: String,
        var correctAnswersCount: Int = 0
    )

    data class Question(
        val question: Words,
        val answer: List<Words>,
    )

    val dictionary = loadDictionary()

    private fun loadDictionary(): List<Words> {
        try {
            val wordsFile = File(fileName)
            if (!wordsFile.exists()) {
                File("words.txt").copyTo(wordsFile)
            }
            val list: MutableList<Words> = mutableListOf()
            wordsFile.forEachLine { line ->
                val words = line.split('|')
                val original = words[0]
                val description = words[1]
                val countAnswer: Int = words.getOrNull(2)?.toIntOrNull() ?: 0
                list.add(Words(original, description, countAnswer))
            }
            return list
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Некорректный файл")
        }
    }

    fun saveDictionary(dictionary: List<Words>, fileName: String) {
        val file = File(fileName)
        file.writeText(dictionary.joinToString("\n") { "${it.original}|${it.description}|${it.correctAnswersCount}" })
    }

    fun guessingWords(words: List<Words>) {
        val countNotLearnedWords = words.count { it.correctAnswersCount < COUNT_ANSWER }

        while (true) {
            val notLearningWords = words.filter { it.correctAnswersCount < COUNT_ANSWER }
            if (notLearningWords.isEmpty()) {
                println("Все слова в словаре выучены")
                return
            }
            val questionWords = if (notLearningWords.count() > COUNT_ANSWER) {
                words.filter { it.correctAnswersCount < COUNT_ANSWER }
                    .shuffled()
                    .take(COUNT_QUESTION)
            } else {
                (notLearningWords + words.filter { it.correctAnswersCount > COUNT_ANSWER }
                    .shuffled()
                    .take(COUNT_QUESTION - countNotLearnedWords)
                        ).shuffled()
            }

            val correctAnswer = questionWords.random()

            val correctAnswerId = questionWords.indexOf(correctAnswer)
            println("${correctAnswer.original}:")
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
                saveDictionary(words, "words.txt")
            } else {
                println("Не правильно! ${correctAnswer.original} - это ${correctAnswer.description}")
            }
        }
    }

    fun getStatistic(data: List<Words>): String {
        val totalCount = data.count()
        val learnedCount = data.filter { it.correctAnswersCount >= COUNT_ANSWER }
            .count()
        val percent = (learnedCount.toDouble() / totalCount.toDouble() * 100.00).toInt()
        return "Выучено $learnedCount из $totalCount слов | $percent %"
    }

    fun question(): Question {
        val dictionary = dictionary
        val countNotLearnedWords = dictionary.count { it.correctAnswersCount < COUNT_ANSWER }
        val notLearningWords = dictionary.filter { it.correctAnswersCount < COUNT_ANSWER }
        val questionWords = if (notLearningWords.count() > COUNT_ANSWER) {
            dictionary.filter { it.correctAnswersCount < COUNT_ANSWER }
                .shuffled()
                .take(COUNT_QUESTION)
        } else {
            (notLearningWords + dictionary.filter { it.correctAnswersCount > COUNT_ANSWER }
                .shuffled()
                .take(COUNT_QUESTION - countNotLearnedWords)
                    ).shuffled()
        }
        val correctAnswer = questionWords.random()
        return Question(correctAnswer, questionWords)
    }

    fun checkAnswer(answerId: Int, question: Question): Boolean {
        return question.answer.getOrNull(answerId)?.original == question.question.original
    }
}

const val COUNT_ANSWER = 3
const val COUNT_QUESTION = 4