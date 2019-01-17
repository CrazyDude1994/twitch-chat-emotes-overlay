package com.exception.catcher.twitchtvemotes.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


class PhoneActivity : AppCompatActivity() {

    private val mainPresenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainPresenter.onCreate(this)
    }
}
