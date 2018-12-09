package com.exception.catcher.twitchtvemotes.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.exception.catcher.twitchtvemotes.ChatService
import com.exception.catcher.twitchtvemotes.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.start_button)
        val stopButton = findViewById<Button>(R.id.stop_button)
        val channelEditText = findViewById<EditText>(R.id.channel_name)
        val spinner = findViewById<Spinner>(R.id.position_spinner)
        val height = findViewById<SeekBar>(R.id.height)
        val width = findViewById<SeekBar>(R.id.width)

        height.max = 100
        width.max = 100

        val preferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

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
                this,
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
