package com.example.learning.models

data class WordResponse(
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>?,
    val meanings: List<Meaning>?
)

data class Phonetic(
    val text: String?
)

data class Meaning(
    val partOfSpeech: String?,
    val definitions: List<Definition>?
)

data class Definition(
    val definition: String?,
    val example: String?
)