package docs.pro.generics

class Generics {
}

//fun <T> List<T>.slice(indexes: IntRange): List<T>

fun main() {
    val letters = ('a'..'z').toList()
    println(letters.slice<Char>(0 .. 2))

    val authors = listOf("Dmitry", "Alice", "Bob")
    val readers = mutableSetOf<String>("Alice", "Carol")

    println(readers.filter { it !in authors })

// TODO: проверить код
}