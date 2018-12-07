package com.exception.catcher.twitchtvemotes.api

import com.exception.catcher.twitchtvemotes.models.EmoteModel
import io.reactivex.Flowable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern


class EmoteApi(val channel: String) {

    private var bttvService: BTTVService
    private var ffzService: FFZService

    init {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())

        bttvService = retrofit.baseUrl("https://api.betterttv.net").build().create(BTTVService::class.java)
        ffzService = retrofit.baseUrl("https://api.frankerfacez.com").build().create(FFZService::class.java)
    }

    fun getGlobalEmotes(): Flowable<List<EmoteModel>> {

        val bttvFlowable = bttvService.getGlobalEmotes().map {
            it.data.map {
                EmoteModel(
                    it.code,
                    "https://cdn.betterttv.net/emote/${it.id}/3x"
                )
            }
        }

        val bttvChannelFlowable = bttvService.getChannelEmotes(channel).map {
            it.data.map {
                EmoteModel(
                    it.code,
                    "https://cdn.betterttv.net/emote/${it.id}/3x"
                )
            }
        }

        val ffzChannelFlowable = ffzService.getChannelEmotes(channel)
            .map {
                it.data.values.map { it.emotes.map { EmoteModel(it.name, "https:${it.urls.values.last()}") } }.flatten()
            }

        val ffzGlobalFlowable = ffzService.getGlobalEmotes()
            .map {
                it.data.values.map { it.emotes.map { EmoteModel(it.name, "https:${it.urls.values.last()}") } }.flatten()
            }

        return Flowable.merge(
            listOf(
                bttvFlowable,
                bttvChannelFlowable,
                ffzChannelFlowable,
                ffzGlobalFlowable
            )
        )
    }
}