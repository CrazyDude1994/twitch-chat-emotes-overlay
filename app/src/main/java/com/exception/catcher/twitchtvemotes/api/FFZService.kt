package com.exception.catcher.twitchtvemotes.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path

interface FFZService {

    @GET("v1/room/{channel}")
    fun getChannelEmotes(@Path("channel") channel: String): Flowable<FFZEmoteResponse>
    @GET("v1/set/global")
    fun getGlobalEmotes(): Flowable<FFZEmoteResponse>
}

data class FFZEmoteResponse(@SerializedName("sets") val data: Map<String, FFZSet>)
data class FFZSet(@SerializedName("emoticons") val emotes: List<FFZEmote>)
data class FFZEmote(@SerializedName("name") val name: String, @SerializedName("urls") val urls: Map<String, String>)