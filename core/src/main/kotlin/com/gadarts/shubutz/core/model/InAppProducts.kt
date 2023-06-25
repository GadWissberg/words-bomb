package com.gadarts.shubutz.core.model

import com.gadarts.shubutz.core.model.assets.definitions.TexturesDefinitions

enum class InAppProducts(
    val label: String,
    val icon: TexturesDefinitions,
    val amount: Int,
    val applyAnimation: Boolean = true,
    val flashEffect: Boolean = false
) {
    PACK_0("אוסף של %s מטבעות", TexturesDefinitions.ICON_PACK_1, 9, false),
    PACK_1("שק של %s מטבעות", TexturesDefinitions.ICON_PACK_2, 18),
    PACK_2("תיבה של %s מטבעות", TexturesDefinitions.ICON_PACK_3, 36, flashEffect = true),
}
