package org.example
import java.io.File

fun main() {
    val wordsFile: File = File("words.txt")
    val dictionary: MutableList<Words> = mutableListOf()

    wordsFile.forEachLine { line ->
        val words = line.split('|')
        val original = words[0]
        val description = words[1]
        val countAnswer: Int = words.getOrNull(2)?.toIntOrNull() ?: 0
        dictionary.add(Words(original, description, countAnswer))
    }

    dictionary.forEach { println(it) }
}

data class Words(
    val original: String,
    val description: String,
    val correctAnswersCount: Int = 0
)