# Null - безопасность

Примеры кода:

### Сокращение для “Если не null”

```kotlin
val files = File("Test").listFiles()
println(files?.size) // размер выводится, если размер файлов не равен null
```

### Сокращение для “Если не null, иначе”

```kotlin
val files = File("Test").listFiles()

println(files?.size ?: "empty") // если файл равен null, выводится "empty"

// Чтобы вычислить резервное значение в блоке кода, используйте команду `run`
val filesSize = files?.size ?: run {
    return someSize
}
println(filesSize)
```

### Выброс исключения при равенстве null

```kotlin
val values = ...
val email = values["email"] ?: throw IllegalStateException("Email is missing!")
```

### Получение первого элемента, возможно, пустой коллекции

```kotlin
val emails = ... // может быть пустой
val mainEmail = emails.firstOrNull() ?: ""
```

### Выполнение при неравенстве null

```kotlin
val value = ...

value?.let {
    ... // этот блок выполняется, если value не равен null
}
```

