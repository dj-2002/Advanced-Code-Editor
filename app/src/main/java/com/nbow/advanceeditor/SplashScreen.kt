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

class SplashScreen : AppCompatActivity() {

    private val TAG = "SplashScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        if(savedInstanceState==null) {
            lifecycleScope.launch(Main){

                val intent = Intent(this@SplashScreen, MainActivity::class.java)
                delay(500)
                Log.e(TAG, "onCreate: above start main Activity")
                startActivity(intent)
                Log.e(TAG, "onCreate: below start main Activity")
                finish()
            }
        }

    }
}