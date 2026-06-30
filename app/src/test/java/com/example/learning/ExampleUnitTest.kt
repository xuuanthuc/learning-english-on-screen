package com.example.learning

import com.example.learning.models.CsvTrans
import com.example.learning.models.WordData
import com.example.learning.models.toWord
import com.example.learning.tools.RetrofitClient
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.collections.forEach

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        fetchWord()
    }

    suspend fun fetchWord() {
        try {
            val paths = listOf<String>(
//                "cefr_english_B1",
//                "cefr_english_B2",
                "cefr_english_C1",
                "cefr_english_C2"
            )

            paths.forEach { p ->
                val results = mutableListOf<WordData>()
                val file = File("src/test/resources/$p.csv")
                val rows: List<List<String>> = csvReader().readAll(file)

                val words: List<CsvTrans> = rows.map { row ->
                    CsvTrans(
                        english = row[0].trim(),
//                        vietnamese = row[1].trim()
                    )
                }

                words.forEach { w ->
                    delay(1000)
                    val first = try {
                        val response = RetrofitClient.api.getWord(w.english)
                        response.firstOrNull()?.toWord()
                    } catch (_: Exception) {
                        null
                    }

                    if (first != null) {
                        println(first)
                        results.add(first)
                    }
                }

                val gson = Gson()
                val json = gson.toJson(results)
                val newJsonData = File("build/test-output/${p}.json")
                newJsonData.parentFile?.mkdirs()
                newJsonData.writeText(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}