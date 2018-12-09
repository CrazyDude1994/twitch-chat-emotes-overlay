package com.exception.catcher.twitchtvemotes.models

import java.util.regex.Pattern

data class EmoteModel(
    val name: String,
    val url: String,
    val pattern: Pattern = Pattern.compile("\\b(" + Pattern.quote(name) + ")\\b")
)