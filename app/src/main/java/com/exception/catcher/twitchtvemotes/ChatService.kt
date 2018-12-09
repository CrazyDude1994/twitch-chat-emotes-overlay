package com.exception.catcher.twitchtvemotes

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.exception.catcher.twitchtvemotes.api.EmoteApi
import com.exception.catcher.twitchtvemotes.ui.ChatAdapter
import com.exception.catcher.twitchtvemotes.ui.Message
import com.exception.catcher.twitchtvemotes.ui.TwitchEmote
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.cap.EnableCapHandler
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent
import org.pircbotx.hooks.events.MessageEvent

class ChatService : Service(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var recyclerView: RecyclerView
    private val chatAdapter = ChatAdapter()
    private lateinit var windowManager: WindowManager
    private lateinit var layoutInflater: LayoutInflater
    private lateinit var parent: View
    private var botX: PircBotX? = null
    private var subscribe: Disposable? = null
    private var thread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let {
            if (botX == null) {
                val channelName = it.getStringExtra("channel_name")
                initChat(channelName)
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(parent)
        botX?.stopBotReconnect()
        botX?.close()
        thread?.interrupt()
        subscribe?.dispose()
        botX = null
        subscribe = null
        thread = null
        val preferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        preferences.unregisterOnSharedPreferenceChangeListener(this)
    }


    override fun onCreate() {
        super.onCreate()
        layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private fun generateParams(position: Int, height: Int, width: Int): WindowManager.LayoutParams {
        val newWidth = (resources.displayMetrics.widthPixels * width) / 100
        val newHeight = (resources.displayMetrics.heightPixels * height) / 100
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                    newWidth,
                    newHeight,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    0,
                    PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                    newWidth,
                    newHeight,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    0,
                    PixelFormat.TRANSLUCENT
            )
        }.also {
            it.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            when (position) {
                0 -> {
                    it.gravity = Gravity.TOP or Gravity.START
                }
                1 -> {
                    it.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                }
                2 -> {
                    it.gravity = Gravity.TOP or Gravity.END
                }
                3 -> {
                    it.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                }
                4 -> {
                    it.gravity = Gravity.CENTER_VERTICAL or Gravity.END
                }
                5 -> {
                    it.gravity = Gravity.BOTTOM or Gravity.START
                }
                6 -> {
                    it.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                }
                7 -> {
                    it.gravity = Gravity.BOTTOM or Gravity.END
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, data: String?) {
        val position = sharedPreferences?.getInt("position", 0) ?: 0
        val width = sharedPreferences?.getInt("width", 25) ?: 0
        val height = sharedPreferences?.getInt("height", 25) ?: 0
        windowManager.updateViewLayout(parent, generateParams(position, height, width))
    }

    private fun initChat(channelName: String) {
        val preferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        parent = layoutInflater.inflate(R.layout.chat_recycler, null)
        recyclerView = parent.findViewById(R.id.chat_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
        windowManager.addView(parent, generateParams(preferences.getInt("position", 0),
                preferences.getInt("height", 0), preferences.getInt("width", 0)))

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
                                                    emptyList(), "white"
                                            )
                                    )
                                    recyclerView.adapter?.itemCount?.let { recyclerView.smoothScrollToPosition(it) }
                                }
                    }

                    override fun onMessage(event: MessageEvent) {
                        super.onMessage(event)
                        val emotes = event.tags["emotes"] ?: ""
                        var color = event.tags["color"] ?: "white"
                        if (color.isEmpty()) {
                            color = "white"
                        }
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
                                                twitchEmotes, color
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

        botX = PircBotX(configuration)

        thread = Thread(Runnable {
            botX?.startBot()
        })
        thread?.start()

        subscribe = EmoteApi(channelName).getGlobalEmotes()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    chatAdapter.addEmotes(it)
                }, {

                })
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}