package com.nbow.advanceeditor

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.nbow.advanceeditor.databinding.ActivityFormatBinding
import com.nbow.advanceeditor.databinding.ActivityMainBinding

import android.graphics.Typeface

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.text.getSpans
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.content.res.ResourcesCompat
import top.defaults.colorpicker.ColorPickerPopup
import android.R.layout
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.text.*
import android.text.style.*
import android.view.*
import android.widget.PopupWindow

import android.widget.TextView
import androidx.annotation.FontRes
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.selects.select


class FormatActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private  val TAG = "FormatActivity"

    private lateinit var binding: ActivityFormatBinding
    private var selectedFont: Int =R.font.arial
    private var isBoldEnabled = false
    private  var isItalicEnabled = false
    private var isStrikethroughEnabled = false
    private var isUnderlineEnabled = false
    private var isColorTextEnabled = false
    private var isAlignCenterEnabled = false
    private var isAlignLeftEnabled = true
    private var isAlignRightEnabled = false

    val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Format Mode (BETA VERSION)")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        binding.editText.typeface=ResourcesCompat.getFont(applicationContext,R.font.opensans)

        binding.editText.doOnTextChanged{
            text, start, before, count ->
            run {
                Log.e(TAG, "onCreate: $text $start $before $count ")

                if(isBoldEnabled)
                    binding.editText.text.apply {
                        this.setSpan(StyleSpan(Typeface.BOLD), start,start+count,flag)
                    }
                if(isItalicEnabled)
                    binding.editText.text.apply {
                        this.setSpan(StyleSpan(Typeface.ITALIC), start,start+count,flag)
                    }
                if(isUnderlineEnabled)
                    binding.editText.text.apply {
                        this.setSpan(UnderlineSpan(), start,start+count,flag)
                    }
                if(isStrikethroughEnabled)
                    binding.editText.text.apply {
                        this.setSpan(StrikethroughSpan(), start,start+count,flag)
                    }
//                binding.editText.apply {
//                    var startIndex = selectionStart-1
//                    var endIndex = this.text.indexOf('\n',selectionEnd-1)
//                    while(startIndex>0 && this.text[startIndex]!='\n'){
//                        startIndex--
//                    }
//                    if(endIndex==-1) endIndex = this.text.length-1
//
//
//                    if(isAlignCenterEnabled) this.text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),startIndex,endIndex,flag)
//                    else if(isAlignLeftEnabled) this.text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),startIndex,endIndex,flag)
//                    else if(isAlignRightEnabled) this.text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),startIndex,endIndex,flag)
//                }

                val typeface = Typeface.create(ResourcesCompat.getFont(applicationContext,selectedFont),Typeface.NORMAL)
                binding.editText.text.apply { this.setSpan(CustomTypefaceSpan(typeface),start,start+count,flag) }
            }

        }



        binding.textEditorBottam.apply {

            bold.setOnClickListener({
                isBoldEnabled=!isBoldEnabled

                binding.textEditorBottam.bold.apply {
                    if(isBoldEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }

                changeSelectedTextStyle(bold=true)

            })
            italic.setOnClickListener({
                isItalicEnabled=!isItalicEnabled
                binding.textEditorBottam.italic.apply {

                    if(isItalicEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }
                changeSelectedTextStyle(italic = true)
//                binding.editText.apply {
//                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart,selectionEnd , flag)
//                }
            })
            underline.setOnClickListener({
                isUnderlineEnabled = !isUnderlineEnabled
                binding.textEditorBottam.underline.apply {
                    if(isUnderlineEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }
                changeSelectedTextStyle(underline = true)
//                binding.editText.apply {
//                    text.setSpan(UnderlineSpan(), selectionStart,selectionEnd , flag)
//                }
            })
            strikethrough.setOnClickListener({
                isStrikethroughEnabled = !isStrikethroughEnabled
                binding.textEditorBottam.strikethrough.apply {
                    if(isStrikethroughEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }
                changeSelectedTextStyle(strikethrough = true)
//                binding.editText.apply {
//                    text.setSpan(StrikethroughSpan(), selectionStart,selectionEnd , flag)
//                }
            })
            alignCenter.setOnClickListener({
                changeAlignmentValue(center = true)
                changeParagraphStyle(alignCenter = true)
                binding.editText.apply {

                }
            })

            alignLeft.setOnClickListener({
                changeAlignmentValue(left = true)
                changeParagraphStyle(alignLeft = true)
                isAlignLeftEnabled = !isAlignLeftEnabled
            })
            alignRight.setOnClickListener({
                changeAlignmentValue(right = true)
                changeParagraphStyle(alignRight = true)
                isAlignRightEnabled = !isAlignRightEnabled
            })
            colorText.setOnClickListener({
//                isColorTextEnabled = !isColorTextEnabled
//                binding.textEditorBottam.colorText.apply {
//                    if(isColorTextEnabled) setBackgroundColor(Color.GREEN)
//                    else setBackgroundColor(Color.GRAY)
//                }
//                changeSelectedTextStyle(colorText = true)

                pickColor()
//                binding.editText.apply {
//                    text.setSpan(ForegroundColorSpan(Color.BLUE), selectionStart,selectionEnd , flag)
//                }
            })
            textFont.setOnClickListener({
                binding.editText.apply {
                    showFontSelectionPopUp()
                }
            })


        }


    }

    fun changeAlignmentValue(left:Boolean = false ,right:Boolean = false ,center:Boolean = false){
        isAlignLeftEnabled = left
        isAlignRightEnabled = right
        isAlignCenterEnabled = center
    }

    fun getCurrentCursorLine(selectionPosition : Int):Int
    {

        Log.e(TAG, "getCurrentCursorLine: $selectionPosition", )
        val layout = binding.editText.layout

        if(selectionPosition != -1)
        {
            return layout.getLineForOffset(selectionPosition)
        }
        return -1;
    }

    fun changeSelectedTextStyle(bold : Boolean = false,italic: Boolean = false,underline : Boolean = false,strikethrough : Boolean = false,colorText : Boolean = false){
        binding.editText.apply {


            if(selectionEnd != selectionStart) {

                if((bold && !isBoldEnabled) || (italic && !isItalicEnabled) || (strikethrough && !isStrikethroughEnabled) || (colorText && !isColorTextEnabled) || (underline && !isUnderlineEnabled)) {
                    var next: Int

                    var i = selectionStart
                    while (i < selectionEnd) {

                        // find the next span transition
                        next = text.nextSpanTransition(i, selectionEnd, CharacterStyle::class.java)

                        val spans: Array<CharacterStyle> = text.getSpans(i, next, CharacterStyle::class.java)

                        for (span in spans) {

                            if (span is StyleSpan) {
                                val spn = span as StyleSpan
                                if ((spn.style == Typeface.BOLD && bold) || (spn.style == Typeface.ITALIC && italic))
                                    text.removeSpan(spn)
                            }else if((span is UnderlineSpan && underline) || (span is StrikethroughSpan && strikethrough) || (span is ForegroundColorSpan && colorText)){
                                text.removeSpan(span)
                            }
                        }
                        i = next
                    }

                }else if(bold)
                    text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, flag)
                else if(italic)
                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart, selectionEnd, flag)
                else if(underline)
                    text.setSpan(UnderlineSpan(), selectionStart, selectionEnd, flag)
                else if(strikethrough)
                    text.setSpan(StrikethroughSpan(), selectionStart, selectionEnd, flag)
                else if(colorText)
                    text.setSpan(ForegroundColorSpan(Color.BLUE), selectionStart,selectionEnd , flag)

            }
        }
    }

    fun changeParagraphStyle(alignCenter:Boolean=false,alignLeft:Boolean=false,alignRight:Boolean = false,){
        binding.editText.apply {
            if(selectionEnd != selectionStart) {

                if( (alignCenter  || alignLeft || alignRight)  ) {
                    var next: Int

                    var i = selectionStart
                    while (i < selectionEnd) {

                        // find the next span transition
                        next = text.nextSpanTransition(i, selectionEnd, ParagraphStyle::class.java)

                        val spans: Array<ParagraphStyle> = text.getSpans(i, next, ParagraphStyle::class.java)

                        for (span in spans) {

                            if (span is AlignmentSpan) {
                                text.removeSpan(span)
                            }
                        }
                        i = next
                    }
                }

            }
//            Log.e(TAG, "changeParagraphStyle: ${getCurrentCursorLine()}", )
            
            binding.editText.apply {
                val currentLine=getCurrentCursorLine(selectionStart)
                val endLine = getCurrentCursorLine(selectionEnd)
                var start = this.layout.getLineStart(currentLine)
                var end = this.layout.getLineEnd(endLine)
                Log.e(TAG, "changeParagraphStyle: $start $end" )
                if(end==-1) end = binding.editText.text.length
                if(alignCenter) {
                    text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),start,end,flag)
                }
                else if(alignLeft) text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),start,end,flag)
                else if(alignRight) text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),start,end,flag)
            
            }
        }
    }

    private fun initFontPopUpMenu(popup: PopupMenu, res:Int, fontId : Int,title : String)
    {
        var menuItem  = popup.menu.findItem(res)
        val ss = SpannableStringBuilder(title)
        val typeface = Typeface.create(ResourcesCompat.getFont(applicationContext,fontId),Typeface.NORMAL)
        ss.setSpan(CustomTypefaceSpan(typeface),0,ss.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        menuItem.title=SpannableString(ss)
    }

    private fun showFontSelectionPopUp() {

        val popup = androidx.appcompat.widget.PopupMenu(this, binding.textEditorBottam.textFont)
        popup.inflate(R.menu.font_selection_menu)
        initFontPopUpMenu(popup,R.id.helvetica_bold,R.font.helvetica_bold,"Helvetica bold")
        initFontPopUpMenu(popup,R.id.helvetica,R.font.helvetica,"Helvetica")
        initFontPopUpMenu(popup,R.id.georgia,R.font.georgia,"Georgia")
        initFontPopUpMenu(popup,R.id.opensans,R.font.opensans,"OpenSans")
        initFontPopUpMenu(popup,R.id.raleway,R.font.raleway,"Raleway")
        initFontPopUpMenu(popup,R.id.arial,R.font.arial,"Arial")
        initFontPopUpMenu(popup,R.id.calibri,R.font.helvetica,"Calibri")
        initFontPopUpMenu(popup,R.id.verdana,R.font.verdana,"Verdana")
        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {

                R.id.georgia -> {
                    applyFontEdittext(R.font.georgia)
                }
                R.id.arial->{
                    applyFontEdittext(R.font.arial)
                }
                R.id.helvetica_bold->{
                   applyFontEdittext(R.font.helvetica_bold)
                }
                R.id.helvetica->{
                    applyFontEdittext(R.font.helvetica)
                }
                R.id.opensans->{
                    applyFontEdittext(R.font.opensans)
                }
                R.id.raleway->{
                    applyFontEdittext(R.font.raleway)
                }
                R.id.verdana->{
                    applyFontEdittext(R.font.verdana)
                }
                R.id.calibri->{
                    applyFontEdittext(R.font.calibri)
                }
            }
            false
        }
        popup.show()

    }

    private fun applyFontEdittext(fontRes: Int) {
        binding.editText.apply {
            if(selectionStart!=selectionEnd) {
                val myTypeface = Typeface.create(
                    ResourcesCompat.getFont(context, fontRes),
                    Typeface.NORMAL
                )
                (text as Spannable).setSpan(
                    CustomTypefaceSpan(myTypeface),
                    selectionStart,
                    selectionEnd,
                    flag
                )
            }
            else
            {
                selectedFont=fontRes
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
       return false
    }

    fun pickColor() {
        val v:View = binding.editText
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(false) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(false)
            .build()
            .show(v, object : ColorPickerPopup.ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    binding.editText.text.setSpan(ForegroundColorSpan(color), binding.editText.selectionStart,binding.editText.selectionEnd , flag)
                }
                fun onColor(color: Int, fromUser: Boolean) {}
            })


    }

}


