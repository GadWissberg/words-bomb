package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.google.gson.JsonObject

class WordsLoader {
    private val gson = Gson()

    fun load(): HashMap<String, ArrayList<String>> {
        val result = HashMap<String, ArrayList<String>>()
        val reader = Gdx.files.local(WORDS_JSON_FILE_PATH).reader()
        val jsonObject = gson.fromJson(reader, JsonObject::class.java)
        val wordsByCategoryJsonArray = jsonObject.get(JSON_KEY_WORDS_BY_CATEGORY).asJsonArray
        wordsByCategoryJsonArray.forEach {
            val asJsonObject = it.asJsonObject
            val category = asJsonObject.get(JSON_KEY_CATEGORY).asString
            val list = ArrayList<String>()
            asJsonObject.getAsJsonArray(JSON_KEY_WORDS)
                .forEach { wordJsonElement ->
                    list.add(wordJsonElement.asString)
                }
            result[category] = list
        }
        return result
    }

    companion object {
        const val JSON_KEY_WORDS_BY_CATEGORY = "words_by_category"
        const val JSON_KEY_CATEGORY = "category"
        const val JSON_KEY_WORDS = "words"
        const val WORDS_JSON_FILE_PATH = "assets/words.json"
    }
}
