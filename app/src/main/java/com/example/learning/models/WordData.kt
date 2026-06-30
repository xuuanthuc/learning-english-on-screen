package com.example.learning.models

data class WordData(
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>?,
    val meanings: List<Meaning>?,
)

data class WordResponse(
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>?,
    val meanings: List<Meaning>?
)

fun WordResponse.toWord(): WordData {
    return WordData(
        word = this.word,
        phonetic = this.phonetic,
        phonetics = this.phonetics,
        meanings = this.meanings.orEmpty()
    )
}

data class Phonetic(
    val text: String?,
    val audio: String?,
    val sourceUrl: String?,
    val license: License?,
)

fun Phonetic.toEntity(wordId: String): PhoneticEntity {
    return PhoneticEntity(
        wordId = wordId,
        text = text,
        audio = audio,
        sourceUrl = sourceUrl,
        licenseName = license?.name,
        licenseUrl = license?.url
    )
}

data class Meaning(
    val partOfSpeech: String?,
    val definitions: List<Definition>?,
    val vietnamese: String?,
)

data class Definition(
    val definition: String?,
    val example: String?
)

data class License(
    val name: String?,
    val url: String?
)