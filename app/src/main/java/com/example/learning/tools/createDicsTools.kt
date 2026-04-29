package com.example.learning.tools

import kotlinx.coroutines.*

suspend fun fetchWord(word: String) {
    try {
        val response = RetrofitClient.api.getWord(word)
        println("Fetch word")

        val first = response.firstOrNull()

        val ipa = first?.phonetic

        val partOfSpeech = first?.meanings
            ?.firstOrNull { !it.partOfSpeech.isNullOrBlank() }
            ?.partOfSpeech

        val definition = first?.meanings
            ?.flatMap { it.definitions.orEmpty() }
            ?.firstOrNull { !it.definition.isNullOrBlank() }
            ?.definition

        val examples = first?.meanings
            ?.flatMap { it.definitions.orEmpty() }
            .orEmpty().asSequence()
            .mapNotNull { it.example }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)
            .toList()

        println("Word: $word")
        println("IPA: $ipa")
        println("Type: $partOfSpeech")
        println("Meaning: $definition")
        println("Example: $examples")

    } catch (e: Exception) {
        e.printStackTrace()
    }
}