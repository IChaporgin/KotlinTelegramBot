package org.example

fun main() {
        val trainer = try {
        LearnWordsTrainer()
        } catch (e: Exception) {
            println("Не возможно загрузить словарь")
            return
        }

    while (true) {
        println(
            """
            Меню:
            1 - Учить слова
            2 - Статистика
            0 - Выход
        """.trimIndent()
        )

        when (readlnOrNull()?.toIntOrNull()) {
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