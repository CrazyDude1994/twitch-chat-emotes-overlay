package com.exception.catcher.twitchtvemotes.ui

import android.os.Bundle
import android.support.v4.app.FragmentActivity


class TvActivity : FragmentActivity() {

    private val mainPresenter = MainPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainPresenter.onCreate(this)
    }
}
