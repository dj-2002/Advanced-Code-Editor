package com.nbow.advanceeditor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.color
import androidx.core.text.getSpans
import androidx.core.text.set
import androidx.core.text.toSpannable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.w3c.dom.Text
import java.util.HashMap
import android.util.DisplayMetrics




class MyEditText : AppCompatEditText {

    private val TAG = "MyEditText"
    private var rect : Rect
    private var paint : Paint
    private var startIndex : Int = 1

    private var isPrev : Boolean = false
    private var hashMap:HashMap<String,Array<Int>>? = HashMap()
    var textWatcher : TextWatcher = initTextWatcher()
    var hasUnsavedChanges = MutableLiveData<Boolean>(false)
    var extension : String = ""
    private lateinit var lifecycleOwner : LifecycleOwner


    fun setStartIndex(index : Int){
        startIndex = index
    }
    fun setIsPrev(isPrev : Boolean){
        this.isPrev = isPrev
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){

    }

    constructor(context: Context) : super(context)

    init {
        rect = Rect()
        paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = this.currentTextColor
        val metrics = context.resources.displayMetrics
        val dp = 14f
        val fpixels = metrics.density * dp
        paint.setTextSize(fpixels)
        this.addTextChangedListener(textWatcher)
    }

    fun setLifeCycleOwner(lifecycleOwner: LifecycleOwner){
        this.lifecycleOwner = lifecycleOwner
    }

    private fun newLineBeforePosition(s : CharSequence,pos : Int) : Int{
        var position = pos

        if(pos>=0 && pos < s.length) {
            while (position >= 0 && s.get(position) != '\n') {
                position--
            }
            position++

            return position
        }
        return -1
    }

    private fun initTextWatcher() : TextWatcher {
        return object : TextWatcher{

            private var job : Job? = null
            private var startOfLine = -1
            private var endOfLine = -1

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                Log.e(TAG, "beforeTextChanged: start : $start  count : $count after : $after")


            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {


                    Log.e(TAG, "onTextChanged: start : $start before : $before count : $count")

                    if (job?.isActive == true) job!!.cancel()
                    Log.e(TAG, "onTextChanged: job : $job ${job?.isActive}")

                    if (s == null) return
                    hasUnsavedChanges.value = true

                    //                Log.e(TAG, "onTextChanged: startOfLine : $startOfLine endOfLine : $endOfLine change : $change")
//
                    job = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

                        delay(400)
                        if (isActive) {

                            var startOfLine = newLineBeforePosition(s, start - 1)
                            if (startOfLine == -1) {
                                if (start == 0) startOfLine = 0
                                else this.cancel()
                            }

                            var endOfLine = s.indexOf('\n', start + count)
                            if (endOfLine != -1 && endOfLine + 1 < s.length) {
                                endOfLine = s.indexOf('\n', endOfLine + 1)
                            }
                            if (endOfLine == -1) {
                                endOfLine = s.length
                            }
                            if (startOfLine == -1) this.cancel()

                            var change: String = s.substring(startOfLine, endOfLine)

                            if (isActive)
                                setSpannableString(change, startOfLine)
                        }


                }

            }
            override fun afterTextChanged(s: Editable?) {
                Log.e(TAG, "afterTextChanged: called")
            }

        }
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var baseline : Int
        if(isPrev){
            startIndex -= lineCount
            isPrev = false
        }

        for (i in 0 until lineCount) {

            baseline = getLineBounds(i, null)
            canvas.drawText("" + (startIndex + i), rect.left.toFloat(), baseline.toFloat(), paint)
        }

    }

    private fun getFirstWord(text:String,start:Int):Int{
        var index = start+1
        var resultIndex = -1
        val words = text.substring(start).split(' ','\n','\t','/','>')
        var flagStart = false
        while(index<text.length){
            if(text[index]==' ' || text[index]=='\n' || text[index] =='\t' || text[index]=='>' )
            {
                if(flagStart){
                    break
                }
                index++
                continue
            }
            flagStart =true
            index++
        }
        return index
    }


    private fun setSpannableString(textString:String,start:Int = 0){

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val syntaxColor:Boolean = preferences.getBoolean("syntax_color",true)
        Log.e(TAG, ":$syntaxColor ", )
        if(syntaxColor) {

            var spannableStringBuilder = SpannableStringBuilder(textString)
            var color = ForegroundColorSpan(Color.BLACK)
            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean("night_mode_preference", true)
            )
                color = ForegroundColorSpan(Color.WHITE)

            text?.setSpan(color, start, start + textString.length, 0)

            if (hashMap == null) return

            var index = 0

            if ((extension == ".html" || extension == ".htm" || extension == ".xml")) {
                for (line in spannableStringBuilder.split('\n')) {
                    var isString = false
                    var startingQuoteIndex = -1
                    var endingQuoteIndex = -1

                    for (word in line.split(
                        ' ',
                        '\t',
                        '(',
                        ',',
                        ')',
                        '.',
                        '=',
                        '\"',
                        '>',
                        '<'
                    )) {
                        if (index + word.length < spannableStringBuilder.length) {
                            if (spannableStringBuilder.get(index + word.length) == '\"') {
                                if (!isString) {
                                    isString = true
                                    startingQuoteIndex = start + index + word.length
                                } else {
                                    endingQuoteIndex = start + index + word.length + 1
                                }
                            } else if (spannableStringBuilder.get(index + word.length) == '<') {
                                val firstWordLastCharIndex =
                                    getFirstWord(
                                        spannableStringBuilder.toString(),
                                        index + word.length
                                    )
                                if (firstWordLastCharIndex != -1 && firstWordLastCharIndex > index + word.length) {
                                    text?.setSpan(
                                        ForegroundColorSpan(Color.rgb(255, 128, 0)),
                                        start + index + word.length,
                                        start + firstWordLastCharIndex,
                                        0
                                    )
                                }
                            } else if (spannableStringBuilder.get(index + word.length) == '>') {
                                text?.setSpan(
                                    ForegroundColorSpan(Color.rgb(255, 128, 0)),
                                    start + index + word.length,
                                    start + index + word.length + 1,
                                    0
                                )
                            }

                        }

                        if (startingQuoteIndex != -1 && endingQuoteIndex != -1) {
                            text?.setSpan(
                                ForegroundColorSpan(Color.rgb(0, 200, 100)),
                                startingQuoteIndex,
                                endingQuoteIndex,
                                0
                            )
                            isString = false
                            startingQuoteIndex = -1
                            endingQuoteIndex = -1
                        }
                        index += word.length + 1
                    }


                }

            } else {

                for (line in spannableStringBuilder.split('\n')) {
                    var isString = false
                    var startingQuoteIndex = -1
                    var endingQuoteIndex = -1
                    var startingSingleLineCommentIndex = -1

                    for (word in line.split(
                        ' ',
                        '\t',
                        '(',
                        ',',
                        ')',
                        '.',
                        '=',
                        ';',
                        '{',
                        '}',
                        '+',
                        '-',
                        '\"',
                        '/',
                        '*',
                        '>',
                        '<',
                        '#'
                    )) {
                        if (index + word.length < spannableStringBuilder.length) {
                            if (spannableStringBuilder.get(index + word.length) == '\"') {
                                if (!isString) {
                                    isString = true
                                    startingQuoteIndex = start + index + word.length
                                } else {
                                    endingQuoteIndex = start + index + word.length + 1

                                }
                            } else if (extension != ".php" && startingSingleLineCommentIndex == -1 && spannableStringBuilder.get(
                                    index + word.length
                                ) == '/' && index + word.length + 1 < spannableStringBuilder.length && spannableStringBuilder.get(
                                    index + word.length + 1
                                ) == '/'
                            ) {
                                startingSingleLineCommentIndex = start + index + word.length
                            } else if (extension == ".php" && startingSingleLineCommentIndex == -1 && spannableStringBuilder.get(
                                    index + word.length
                                ) == '#'
                            ) {
                                startingSingleLineCommentIndex = start + index + word.length
                            }

                        }

                        if (startingSingleLineCommentIndex == -1 && hashMap!!.containsKey(word)) {
                            text?.setSpan(
                                ForegroundColorSpan(
                                    Color.rgb(
                                        hashMap!!.get(word)!!.get(0),
                                        hashMap!!.get(word)!!.get(1),
                                        hashMap!!.get(word)!!.get(2)
                                    )
                                ), start + index, start + index + word.length, 0
                            )
                        }
                        if (startingQuoteIndex != -1 && endingQuoteIndex != -1) {
                            text?.setSpan(
                                ForegroundColorSpan(Color.rgb(0, 200, 100)),
                                startingQuoteIndex,
                                endingQuoteIndex,
                                0
                            )
                            isString = false
                            startingQuoteIndex = -1
                            endingQuoteIndex = -1
                        }
                        index += word.length + 1
                    }

                    if (startingSingleLineCommentIndex != -1) {
                        text?.setSpan(
                            ForegroundColorSpan(Color.rgb(144, 144, 144)),
                            startingSingleLineCommentIndex,
                            start + index - 1,
                            0
                        )
                    }
                }
            }
        }
    }



    override fun setText(text: CharSequence?, type: BufferType?) {
        Log.e(TAG,"set Text called")
        if(textWatcher!=null)
            removeTextChangedListener(textWatcher)
        super.setText(text?.toSpannable(), BufferType.SPANNABLE)
        setSpannableString(text.toString())
        if(textWatcher!=null)
            addTextChangedListener(textWatcher)
    }

    fun setHashMapValue(){
        hashMap = ExtColorHashMap.getHashMap(extension)
        Log.e(TAG, "setHashMapValue: hashMap $hashMap")
    }

    fun getStartingIndex(): Int {
        return startIndex
    }

}