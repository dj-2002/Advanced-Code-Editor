package com.nbow.advanceeditor

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import com.nbow.advanceeditor.databinding.ActivityFormatBinding
import com.nbow.advanceeditor.databinding.ActivityMainBinding
import android.text.style.URLSpan

import android.text.style.TypefaceSpan

import android.text.style.RelativeSizeSpan

import android.text.style.StrikethroughSpan

import android.text.style.UnderlineSpan

import android.text.style.BackgroundColorSpan

import android.text.style.ForegroundColorSpan

import android.graphics.Typeface
import android.text.Editable

import android.text.style.StyleSpan

import android.text.Spanned
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.text.getSpans
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.text.style.CharacterStyle





class FormatActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private  val TAG = "FormatActivity"

    private lateinit var binding: ActivityFormatBinding
    private var isBoldEnabled = false
    private  var isItalic = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Format Mode (BETA VERSION)")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        navView.setNavigationItemSelectedListener(this)
        drawerLayout.closeDrawer(GravityCompat.START)
        toggle.syncState()

//        s2.setSpan(StyleSpan(Typeface.ITALIC), 0, s2.length(), flag)
//        s3.setSpan(ForegroundColorSpan(Color.RED), 0, s3.length(), flag)
//        s4.setSpan(BackgroundColorSpan(Color.YELLOW), 0, s4.length(), flag)
//        s5.setSpan(UnderlineSpan(), 0, s5.length(), flag)
//        s6.setSpan(StrikethroughSpan(), 0, s6.length(), flag)
//        s7.setSpan(RelativeSizeSpan(2), 0, s7.length(), flag)
//        s8.setSpan(RelativeSizeSpan(0.5f), 0, s8.length(), flag)
//        s9.setSpan(TypefaceSpan("monospace"), 0, s9.length(), flag)
//        s10.setSpan(URLSpan("https://developer.android.com"), 0, s10.length(), flag)

        val textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }

        }
        binding.editText.doOnTextChanged{
            text, start, before, count ->
            run {
                Log.e(TAG, "onCreate: $text $start $before $count ")

                if(isBoldEnabled)
                    binding.editText.text.apply {
                        this.setSpan(StyleSpan(Typeface.BOLD), start,start+count,flag)
                    }
                if(isItalic)
                    binding.editText.text.apply {
                        this.setSpan(StyleSpan(Typeface.ITALIC), start,start+count,flag)
                    }
            }

        }



        binding.textEditorBottam.apply {

            bold.setOnClickListener({
                isBoldEnabled=!isBoldEnabled

                binding.editText.apply {
                    if(selectionEnd != selectionStart) {
//                        val spans = text.getSpans<StyleSpan>(selectionStart,selectionEnd)
//                            for(t in spans)
//                            {
//                                var style = t.style
//                                if(style==Typeface.BOLD)
//                                {
//                                    text.substring(selectionStart,selectionEnd).removeSpan(t)
//                                }
//                            }
                        if(!isBoldEnabled) {
                            var next: Int

                            var i = selectionStart
                            while (i < selectionEnd) {


                                // find the next span transition
                                next = text.nextSpanTransition(
                                    i,
                                    selectionEnd,
                                    CharacterStyle::class.java
                                )

                                // get all spans in this range
//                                var numOfSpans = 0
                                val spans: Array<CharacterStyle> =
                                    text.getSpans(i, next, CharacterStyle::class.java)

//                                val spans: Array<ForegroundColorSpan> = text.getSpans(
//                                    i, selectionEnd,
//                                    ForegroundColorSpan::class.java
//                                )

// loop through spans

// loop through spans
                                for (span in spans) {
//                                    spannableString.removeSpan(span)
                                    if (span is StyleSpan) {
                                        // do something
                                        val spn = span as StyleSpan
                                        if (spn.style == Typeface.BOLD)
                                            text.removeSpan(spn)

                                    }

                                }
//                                for (j in spans.indices) {
//                                    numOfSpans++
//                                }
//                                Log.i("TAG", "spans from $i to $next: $numOfSpans")
                                i = next
                            }

                        }else
                            text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, flag)

                    }
                }
            })
            italic.setOnClickListener({
                isItalic=!isItalic
                binding.editText.apply {
                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart,selectionEnd , flag)
                }
            })
            underline.setOnClickListener({
                binding.editText.apply {
                    text.setSpan(UnderlineSpan(), selectionStart,selectionEnd , flag)
                }
            })
            strikethrough.setOnClickListener({
                binding.editText.apply {
                    text.setSpan(StrikethroughSpan(), selectionStart,selectionEnd , flag)
                }
            })
            alignCenter.setOnClickListener({
                binding.editText.apply {

                }
            })
            alignJustify.setOnClickListener({
                binding.editText.apply {
                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart,selectionEnd , flag)
                }
            })
            alignLeft.setOnClickListener({

            })
            alignRight.setOnClickListener({

            })
            colorText.setOnClickListener({
                binding.editText.apply {
                    text.setSpan(ForegroundColorSpan(Color.BLUE), selectionStart,selectionEnd , flag)
                }
            })
            textFont.setOnClickListener({
                binding.editText.apply {
                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart,selectionEnd , flag)
                }
            })


        }


    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

//        this.menu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
       return false
    }

}