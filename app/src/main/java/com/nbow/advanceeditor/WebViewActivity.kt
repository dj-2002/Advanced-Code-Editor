package com.nbow.advanceeditor

import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewActivity : AppCompatActivity() {
    var mdata = ""
    lateinit var web_view:WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val TAG="Webview"
        setContentView(R.layout.activity_web_view)
         web_view = findViewById(R.id.webView)
       val toolbar:androidx.appcompat.widget.Toolbar=findViewById(R.id.toolbar)
        toolbar.setTitle("Run")
        //toolbar.setNavigationIcon(R.drawable.ic_back_navigation)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        Log.e(TAG, "onCreate: " )

        if (intent != null && savedInstanceState == null) {
            val data: String? = intent.getStringExtra("data")
            Log.e(TAG, "$data ", )
            if (data != null) {
                mdata=data
                loadData()
            }


        }
    }

    private fun loadData() {
        web_view.requestFocus();
        web_view.settings.setSupportZoom(true)
        web_view.settings.domStorageEnabled = true;
        web_view.settings.setJavaScriptEnabled(true);
        web_view.settings.setGeolocationEnabled(true);
        web_view.setSoundEffectsEnabled(true);
        web_view.loadData(
            "$mdata",
            "text/html", "UTF-8"
        );
    }

    override fun onResume() {

        super.onResume()
    }
}


