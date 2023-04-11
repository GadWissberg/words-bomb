package com.gadarts.shubutz.core.model.assets

import com.badlogic.gdx.Gdx
import com.gadarts.shubutz.core.model.Phrase
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.Reader

class PhrasesLoader {
    private val gson = Gson()

    fun load(): HashMap<String, HashMap<String, ArrayList<Phrase>>> {
        val result = HashMap<String, HashMap<String, ArrayList<Phrase>>>()
        val dirHandle = Gdx.files.internal(PHRASES_JSON_FILES_FOLDER_PATH)
        dirHandle.list().forEach {
            loadPhrasesFile(it.file().nameWithoutExtension, it.reader(), result)
        }
        return result
    }

    private fun loadPhrasesFile(
        name: String,
        reader: Reader?,
        result: HashMap<String, HashMap<String, ArrayList<Phrase>>>
    ) {
        val jsonObject = gson.fromJson(reader, JsonObject::class.java)
        val wordsByCategoryJsonArray = jsonObject.get(JSON_KEY_PHRASES_BY_CATEGORY).asJsonArray
        val currentMap = HashMap<String, ArrayList<Phrase>>()
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
                        list.add(Phrase(phraseJsonObject.get(JSON_KEY_PHRASE).asString))
                    }
                }
            currentMap[category] = list
        }
        result[name] = currentMap
    }

    companion object {
        const val JSON_KEY_PHRASES_BY_CATEGORY = "phrases_by_category"
        const val JSON_KEY_CATEGORY = "category"
        const val JSON_KEY_PHRASES = "phrases"
        const val JSON_KEY_PHRASE = "phrase"
        private const val PHRASES_JSON_FILES_FOLDER_PATH = "phrases/"
    }
}
