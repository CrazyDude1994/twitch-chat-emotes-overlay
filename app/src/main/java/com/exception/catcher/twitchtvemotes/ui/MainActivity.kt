package com.exception.catcher.twitchtvemotes.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.exception.catcher.twitchtvemotes.ChatService
import com.exception.catcher.twitchtvemotes.R
import com.exception.catcher.twitchtvemotes.api.EmoteApi
import io.reactivex.android.schedulers.AndroidSchedulers
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.cap.EnableCapHandler
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.events.ConnectEvent
import org.pircbotx.hooks.events.MessageEvent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.start_button)
        val stopButton = findViewById<Button>(R.id.stop_button)
        val channelEditText = findViewById<EditText>(R.id.channel_name)

        startButton.setOnClickListener {
            val intent = Intent(this, ChatService::class.java)
            intent.putExtra("channel_name", channelEditText.text.toString())
            startService(intent)
        }

//        startButton.performClick()

        stopButton.setOnClickListener {
            val intent = Intent(this, ChatService::class.java)
            stopService(intent)
        }
    }
}
