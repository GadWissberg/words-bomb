package com.gadarts.wordsbomb.core

import com.badlogic.gdx.Gdx
import com.gadarts.wordsbomb.core.model.WordObject
import com.google.gson.Gson
import com.google.gson.JsonObject

class WordsLoader {
    private val gson = Gson()

    fun load(): HashMap<String, List<WordObject>> {
        val result = HashMap<String, List<WordObject>>()
        val reader = Gdx.files.local(WORDS_JSON_FILE_PATH).reader()
        val jsonObject = gson.fromJson(reader, JsonObject::class.java)
        val wordsByCategoryJsonArray = jsonObject.get(JSON_KEY_WORDS_BY_CATEGORY).asJsonArray
        wordsByCategoryJsonArray.forEach {
            val asJsonObject = it.asJsonObject
            val category = asJsonObject.get(JSON_KEY_CATEGORY).asString
            val list = ArrayList<WordObject>()
            asJsonObject.getAsJsonArray(JSON_KEY_WORDS)
                .forEach { wordJsonElement ->
                    val wordJsonObject = wordJsonElement.asJsonObject
                    val wordObject = WordObject(
                        wordJsonObject.get(JSON_KEY_WORD).asString,
                        wordJsonObject.get(JSON_KEY_APPEARED).asBoolean
                    )
                    list.add(wordObject)
                }
            result[category] = list
        }
        return result
    }

    companion object {
        const val JSON_KEY_WORDS_BY_CATEGORY = "words_by_category"
        const val JSON_KEY_CATEGORY = "category"
        const val JSON_KEY_WORDS = "words"
        const val JSON_KEY_WORD = "word"
        const val JSON_KEY_APPEARED = "appeared"
        const val WORDS_JSON_FILE_PATH = "assets/words.json"
    }
}
