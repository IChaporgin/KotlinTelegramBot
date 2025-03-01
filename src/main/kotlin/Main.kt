package org.example
import java.io.File

fun main() {

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

    fun getStatistic(data: List<Words>) : String {
        val totalCount = data.count()
        val learnedCount = data.filter { it.correctAnswersCount >= 3 }
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
            1 -> println("Вы выбрали учить слова")
            2 -> {
                println(getStatistic(dictionary))
                readLine()
            }
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}

data class Words(
    val original: String,
    val description: String,
    val correctAnswersCount: Int = 0
)