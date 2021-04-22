package com.boudreau.guitarteacher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.boudreau.guitarteacher.models.WelcomeView

class WelcomeScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val welcomeView = WelcomeView(
                this, intent.extras!!.getInt(LOGGED_IN_USER_ID), savedInstanceState)
        setContentView(welcomeView)
    }
}