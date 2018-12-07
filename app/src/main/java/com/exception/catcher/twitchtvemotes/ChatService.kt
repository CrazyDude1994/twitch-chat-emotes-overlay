package com.exception.catcher.twitchtvemotes

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.exception.catcher.twitchtvemotes.api.EmoteApi
import com.exception.catcher.twitchtvemotes.ui.ChatAdapter
import com.exception.catcher.twitchtvemotes.ui.Message
import com.exception.catcher.twitchtvemotes.ui.TwitchEmote
import io.reactivex.android.schedulers.AndroidSchedulers
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.cap.EnableCapHandler
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent
import org.pircbotx.hooks.events.MessageEvent

class ChatService: Service() {

    private lateinit var recyclerView: RecyclerView
    private val chatAdapter = ChatAdapter()
    private lateinit var windowManager: WindowManager
    private lateinit var layoutInflater: LayoutInflater

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let {
            val channelName = it.getStringExtra("channel_name")
            initChat(channelName)
        }

        return START_REDELIVER_INTENT
    }


    override fun onCreate() {
        super.onCreate()
        layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun initChat(channelName: String) {
        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                500,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                0,
                PixelFormat.TRANSLUCENT)
        } else {
            WindowManager.LayoutParams(
                500,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                0,
                PixelFormat.TRANSLUCENT)
        }

        params.gravity = Gravity.TOP or Gravity.END
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

        val parent = layoutInflater.inflate(R.layout.chat_recycler, null)
        recyclerView = parent.findViewById(R.id.chat_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
        windowManager.addView(parent, params)

        val configuration = Configuration.Builder()
            .addServer("irc.chat.twitch.tv", 6667)
            .setServerPassword("oauth:3s878bc3v00yh9cf4faoro1p8ia7tv")
            .setMessageDelay(0)
            .setName("crazydude1994") //Set the nick of the bot. CHANGE IN YOUR CODE
            .addAutoJoinChannel("#$channelName")
            .addCapHandler(EnableCapHandler("twitch.tv/tags"))
            .addListener(object : ListenerAdapter() {

                override fun onConnect(event: ConnectEvent?) {
                    super.onConnect(event)
                    Handler(Looper.getMainLooper())
                        .post {
                            chatAdapter.addMessage(
                                Message(
                                    "Server",
                                    "Connected",
                                    emptyList()
                                )
                            )
                            recyclerView.adapter?.itemCount?.let { recyclerView.smoothScrollToPosition(it) }
                        }
                }

                override fun onMessage(event: MessageEvent) {
                    super.onMessage(event)
                    val emotes = event.tags["emotes"] ?: ""
                    event.user?.let {
                        Handler(Looper.getMainLooper())
                            .post {
                                val twitchEmotes = ArrayList<TwitchEmote>()
                                val emoteList = emotes.split("/")
                                emoteList.forEach {
                                    val split = it.split(":")
                                    if (split.size == 1)
                                        return@forEach
                                    val id = split[0]
                                    val ranges = split[1].split(",")
                                    ranges.forEach {
                                        val range = it.split("-")
                                        val start = range[0]
                                        val end = range[1]
                                        twitchEmotes.add(
                                            TwitchEmote(
                                                id.toInt(),
                                                start.toInt(),
                                                end.toInt()
                                            )
                                        )
                                    }
                                }
                                val message = Message(
                                    it.nick,
                                    event.message,
                                    twitchEmotes
                                )
                                chatAdapter.addMessage(message)
                                recyclerView.adapter?.itemCount?.let { recyclerView.smoothScrollToPosition(it) }
                            }
                    }
                }
            })
            .setAutoReconnect(false)
            .setAutoSplitMessage(false)
            .buildConfiguration()

        val botX = PircBotX(configuration)
        //Connect to the server
        Thread(Runnable {
            botX.startBot()
        }).start()

        val subscribe = EmoteApi(channelName).getGlobalEmotes()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                chatAdapter.addEmotes(it)
            }, {

            })
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}