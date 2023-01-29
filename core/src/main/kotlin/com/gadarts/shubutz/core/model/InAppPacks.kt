package com.gadarts.shubutz.core.model

import com.gadarts.shubutz.core.model.assets.TexturesDefinitions

enum class InAppPacks(val label: String, val icon: TexturesDefinitions, val amount: Int) {
    PACK_0("אוסף של %s מטבעות - %s", TexturesDefinitions.ICON_PACK_1, 8),
    PACK_1("שק של %s מטבעות - %s", TexturesDefinitions.ICON_PACK_2, 16),
    PACK_2("תיבה של %s מטבעות - %s", TexturesDefinitions.ICON_PACK_3, 32),
}
