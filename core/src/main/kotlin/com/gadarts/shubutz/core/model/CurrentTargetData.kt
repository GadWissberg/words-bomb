package com.gadarts.shubutz.core.model

class CurrentTargetData {
    lateinit var currentPhrase: String
    lateinit var currentCategory: String
    var hiddenLettersIndices = mutableListOf<Int>()
}
