package com.exception.catcher.twitchtvemotes.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Flowable
import retrofit2.http.GET
import retrofit2.http.Path

interface BTTVService {

    @GET("2/emotes")
    fun getGlobalEmotes(): Flowable<BTTVEmoteResponse>

    @GET("2/channels/{channel}")
    fun getChannelEmotes(@Path("channel") channel: String): Flowable<BTTVEmoteResponse>
}

data class BTTVEmoteResponse(@SerializedName("emotes") val data: List<BTTVEmote>)
data class BTTVEmote(@SerializedName("code") val code: String, @SerializedName("id") val id: String)