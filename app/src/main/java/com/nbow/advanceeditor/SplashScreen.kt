package com.nbow.advanceeditor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.security.auth.login.LoginException

class SplashScreen : AppCompatActivity() {

    private val TAG = "SplashScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "onCreate :starting ", )
        val intent=Intent(this@SplashScreen, MainActivity::class.java)
        startActivity(intent)
        Log.e(TAG, "onCreate: finishing", )
        finish()

    }
}