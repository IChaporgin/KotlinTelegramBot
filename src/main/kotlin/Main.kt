package org.example

fun main() {

    val trainer = LearnWordsTrainer()

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
                trainer.guessingWords(trainer.dictionary)
            }
            2 -> {
                println(trainer.getStatistic(trainer.dictionary))
            }
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
    }
}