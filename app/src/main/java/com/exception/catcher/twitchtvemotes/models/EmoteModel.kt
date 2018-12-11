package com.exception.catcher.twitchtvemotes.models

import java.util.regex.Pattern

data class EmoteModel(
    val name: String,
    val url: String,
    val pattern: Pattern = Pattern.compile("(?<=^|\\s)(" + Pattern.quote(name) + ")(?>\$|\\s)"),
    val isOverlay: Boolean = isOverlay(name)
)

fun isOverlay(name: String): Boolean {
    return listOf("SoSnowy", "SantaHat", "CandyCane", "ReinDeer", "IceCold").contains(name)
}

