package com.exception.catcher.twitchtvemotes.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.*
import com.crashlytics.android.Crashlytics
import com.exception.catcher.twitchtvemotes.ChatService
import com.exception.catcher.twitchtvemotes.R
import io.fabric.sdk.android.Fabric

class MainPresenter() {

    fun onCreate(activity: Activity) {
        activity.setContentView(R.layout.activity_main)
        Fabric.with(activity, Crashlytics())

        val startButton = activity.findViewById<Button>(R.id.start_button)
        val stopButton = activity.findViewById<Button>(R.id.stop_button)
        val channelEditText = activity.findViewById<EditText>(R.id.channel_name)
        val spinner = activity.findViewById<Spinner>(R.id.position_spinner)
        val height = activity.findViewById<SeekBar>(R.id.height)
        val width = activity.findViewById<SeekBar>(R.id.width)

        height.max = 100
        width.max = 100

        val preferences = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)

        val position = preferences.getInt("position", 0)
        val currentHeight = preferences.getInt("height", 25)
        val currentWidth = preferences.getInt("width", 25)

        height.progress = currentHeight
        width.progress = currentWidth

        height.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                preferences.edit().putInt("height", progress).apply()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        width.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                preferences.edit().putInt("width", progress).apply()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        ArrayAdapter.createFromResource(
            activity,
            R.array.chat_alignment,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(position)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                preferences.edit().putInt("position", position).apply()
            }
        }

        startButton.setOnClickListener {
            val intent = Intent(activity, ChatService::class.java)
            intent.putExtra("channel_name", channelEditText.text.toString())
            activity.startService(intent)
            try {
                val intent1 = Intent(Intent.ACTION_VIEW, Uri.parse("twitch://stream/" + channelEditText.text))
                activity.startActivity(intent1)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, "Chat is started. Now start the stream", Toast.LENGTH_SHORT).show()
            }
        }

        stopButton.setOnClickListener {
            val intent = Intent(activity, ChatService::class.java)
            activity.stopService(intent)
        }
    }
}