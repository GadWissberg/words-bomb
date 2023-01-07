package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.Gdx
import com.gadarts.shubutz.core.model.Phrase
import com.google.gson.Gson
import com.google.gson.JsonObject

class PhrasesLoader {
    private val gson = Gson()

    fun load(): HashMap<String, ArrayList<Phrase>> {
        val result = HashMap<String, ArrayList<Phrase>>()
        val reader = Gdx.files.local(PHRASES_JSON_FILE_PATH).reader()
        val jsonObject = gson.fromJson(reader, JsonObject::class.java)
        val wordsByCategoryJsonArray = jsonObject.get(JSON_KEY_PHRASES_BY_CATEGORY).asJsonArray
        wordsByCategoryJsonArray.forEach {
            val asJsonObject = it.asJsonObject
            val category = asJsonObject.get(JSON_KEY_CATEGORY).asString
            val list = ArrayList<Phrase>()
            asJsonObject.getAsJsonArray(JSON_KEY_PHRASES)
                .forEach { phraseJsonElement ->
                    if (phraseJsonElement.isJsonPrimitive) {
                        list.add(Phrase(phraseJsonElement.asString))
                    } else {
                        val phraseJsonObject = phraseJsonElement.asJsonObject
                        list.add(Phrase(phraseJsonObject.get(JSON_KEY_PHRASE).asString, true))
                    }
                }
            result[category] = list
        }
        return result
    }

    companion object {
        const val JSON_KEY_PHRASES_BY_CATEGORY = "phrases_by_category"
        const val JSON_KEY_CATEGORY = "category"
        const val JSON_KEY_PHRASES = "phrases"
        const val JSON_KEY_PHRASE = "phrase"
        const val PHRASES_JSON_FILE_PATH = "assets/phrases.json"
    }
}
