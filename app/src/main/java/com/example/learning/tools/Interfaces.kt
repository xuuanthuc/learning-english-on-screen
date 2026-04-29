package com.example.learning.tools

import com.example.learning.models.WordResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun getWord(
        @Path("word") word: String
    ): List<WordResponse>
}