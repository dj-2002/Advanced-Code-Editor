package com.nbow.advanceeditor

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*

import android.view.View.OnLongClickListener
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nbow.advanceeditor.code.AutoCompleteKeywordAdapter
import com.nbow.advanceeditor.code.CodeView
import com.nbow.advanceeditor.syntax.Language
import com.nbow.advanceeditor.syntax.SyntaxManager


class EditorFragment : Fragment {

    private val TAG = "EditorFragment"
    private var editText : CodeView? = null
    private var themeIndex = 2
    private var currentPageIndex : Int = 0
    private var mCurrentLanguage: Language = Language.DEFAULT
    lateinit var fabPrev:FloatingActionButton
    lateinit var fabNext:FloatingActionButton

    var hasUnsavedChanges = MutableLiveData(false)
    var hasLongPress = MutableLiveData<Boolean>(false)
    private var undoRedo=TextViewUndoRedo()
//    private var listOfPageData : MutableList<String> = arrayListOf()

    private var dataFile : DataFile? = null

    fun setDataFile(dataFile: DataFile){
        this.dataFile = dataFile
    }

    fun getEditTextData():StringBuilder{
        val temp = StringBuilder("")
        saveDataToPage()
        if(dataFile!=null){
//            for((count,page) in dataFile!!.listOfPageData.withIndex()){
//                temp.append("$page")
//
//                if(count!=dataFile!!.listOfPageData.size-1) temp.append('\n')
//            }
            for(page in dataFile!!.listOfPageData){
                temp.append(page.toString())
                temp.append('\n')
            }
        }
        return temp
    }
    fun selectAll(){
//        Log.e(TAG, "selectAll: ")
        editText?.requestFocus()
        editText?.selectAll()
    }



    constructor(){
        Log.e(TAG, "constructor of fragment called: $this")
    }

    constructor(dataFile: DataFile,hasUnsavedChanges : Boolean = false){
        this.dataFile = dataFile
        this.hasUnsavedChanges.postValue(hasUnsavedChanges)
    }


    override fun onDestroyView() {
        saveDataToPage()
        super.onDestroyView()
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {

        // data initializing to edit text first time when attach to view
        if(dataFile!=null && currentPageIndex>=0 && currentPageIndex<dataFile!!.listOfPageData.size){
            undoRedo.mIsUndoOrRedo = true
            editText!!.setText(dataFile!!.listOfPageData.get(currentPageIndex))
            undoRedo.mIsUndoOrRedo = false
        }

        if(dataFile!=null) {
            var i=1;
            for (page in dataFile!!.listOfPageData) {
                Log.e(TAG, "onViewStateRestored page $i : size ${page.length}", )
                i++;
            }
        }


        editText?.setOnLongClickListener(OnLongClickListener {
            hasLongPress.value = true
            false
        })

        editText!!.setEnableLineNumber(Utils.isLineNumber)
        editText!!.setLineNumberTextSize(Utils.lineNumbeSize)


        if(dataFile!=null) {
            Log.e(TAG, "onViewStateRestored: ${dataFile!!.fileExtension}", )
            if (dataFile!!.fileExtension == ".cpp")
                mCurrentLanguage = Language.CPP
            else if (dataFile!!.fileExtension == ".c")
                mCurrentLanguage = Language.C
            else if (dataFile!!.fileExtension == ".html" || dataFile!!.fileExtension == ".htm")
                mCurrentLanguage = Language.HTML
            else if (dataFile!!.fileExtension == ".py")
                mCurrentLanguage = Language.PYTHON
            else if (dataFile!!.fileExtension == ".css")
                mCurrentLanguage = Language.CSS
            else if (dataFile!!.fileExtension == ".js")
                mCurrentLanguage = Language.JAVASCRIPT
            else if (dataFile!!.fileExtension == ".php")
                mCurrentLanguage = Language.PHP
//            else if (dataFile!!.fileExtension == ".kt")
//                mCurrentLanguage = Language.KOTLIN
            else if (dataFile!!.fileExtension == ".java")
                mCurrentLanguage = Language.JAVA
            else if(dataFile!!.fileExtension==".txt")
                mCurrentLanguage = Language.TXT

        }
        configLanguageAutoComplete()



        Log.e(TAG, "onViewStateRestored: Language ${mCurrentLanguage.name}", )
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        themeIndex = preferences.getInt("theme_count",1)
        loadTheme()
        //SyntaxManager.applyMonokaiTheme(context, editText, mCurrentLanguage)

        (editText as CodeView).doOnTextChanged { text, start, before, count ->
            hasUnsavedChanges.value = true
        }

        super.onViewStateRestored(savedInstanceState)
    }


     fun changeCodeViewTheme() {
        if (themeIndex >= 4)
            themeIndex = 1
         else
            themeIndex += 1
         loadTheme()
         lifecycleScope.launch(Dispatchers.IO) {
             val preferences = PreferenceManager.getDefaultSharedPreferences(context)
             val editor = preferences.edit()
             editor.putInt("theme_count", themeIndex)
             editor.apply()
         }
    }

     private fun loadTheme() {
        when (themeIndex) {
            1 -> {
                SyntaxManager.applyMonokaiTheme(context, editText, mCurrentLanguage)
                //Toast.makeText(context, "Monokai", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                SyntaxManager.applyNoctisWhiteTheme(context, editText, mCurrentLanguage)
                //Toast.makeText(context, "Noctis White", Toast.LENGTH_SHORT).show()
            }
            3 -> {
                SyntaxManager.applyFiveColorsDarkTheme(context, editText, mCurrentLanguage)
                //Toast.makeText(context, "5 Colors Dark", Toast.LENGTH_SHORT).show()
            }
            else -> {
                SyntaxManager.applyOrangeBoxTheme(context, editText, mCurrentLanguage)
                //Toast.makeText(context, "Orange Box", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun configLanguageAutoComplete() {

        Log.e(TAG, "configLanguageAutoComplete: Language ${mCurrentLanguage.name}", )

        val languageKeywords: Array<String>
        languageKeywords = when (mCurrentLanguage) {
            Language.JAVA -> resources.getStringArray(R.array.java_keywords)
            Language.PYTHON -> resources.getStringArray(R.array.python_keywords)
            Language.C -> resources.getStringArray(R.array.c_keywords)
            Language.CPP -> resources.getStringArray(R.array.cpp_keywords)
            Language.JAVASCRIPT -> resources.getStringArray(R.array.javascript_keywords)
            Language.HTML -> resources.getStringArray(R.array.html_keywords)
//            Language.XML -> resources.getStringArray(R.array.html_keywords)
            Language.GO_LANG -> resources.getStringArray(R.array.go_keywords)
            Language.PHP -> resources.getStringArray(R.array.php_keywords)
            else -> resources.getStringArray(R.array.blank)
        }

        //Custom list item xml layout
        val layoutId = R.layout.suggestion_list_item

        //TextView id to put suggestion on it
        val viewId = R.id.suggestItemTextView
        if(mCurrentLanguage==Language.HTML){
            val adapter = AutoCompleteKeywordAdapter(requireContext().applicationContext, languageKeywords.toMutableList())
            //Add Custom Adapter to the CodeView
            editText!!.setAdapter(adapter)
        }else{
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(requireContext().applicationContext, layoutId, viewId, languageKeywords)
            //Add Custom Adapter to the CodeView
            editText!!.setAdapter(adapter)
        }
    }


    override fun onResume() {
//        val PREFERENCE_NAME="myPreference"
        val KEY_TEXT_SIZE = "TEXT_SIZE_PREFERENCE"
        val preference= PreferenceManager.getDefaultSharedPreferences(context)
        var myTextSize:Int = preference.getInt(KEY_TEXT_SIZE,16)
        editText?.setTextSize(myTextSize.toFloat())
        val  f=(preference.getString("font_family","DEFAULT"))
        val  pTheme =preference.getInt("theme_count",1)
        if(pTheme!=themeIndex)
        {
            themeIndex=pTheme
            loadTheme()
        }

        if(f!="DEFAULT"){
            if(f=="DEFAULT_BOLD")
                editText?.typeface= Typeface.DEFAULT_BOLD
            else if(f=="MONOSPACE")
                editText?.typeface= Typeface.MONOSPACE
            else if(f=="SANS_SARIF")
                editText?.typeface= Typeface.SANS_SERIF
            else if(f=="SERIF")
                editText?.typeface= Typeface.SERIF
        }
        Log.e(TAG, "onResume: ", )
        super.onResume()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val KEY_WRAP = "word_wrap"
        val preference= PreferenceManager.getDefaultSharedPreferences(context)
        val isWrap = preference.getBoolean(KEY_WRAP,false)
        var layout = R.layout.fragment_editor


        if(!isWrap)
          layout = R.layout.fragment_editor_unwrap

        val view = inflater.inflate(layout, container, false)

       //createPagesFromListOfLines()

        currentPageIndex = 0
        editText = view.findViewById(R.id.editText)

//            Log.e(TAG, "onCreateView: observe called value of unsaved change $it ${hasUnsavedChanges.value}")

//        editText?.inputType =(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//        editText?.isSingleLine = false


//        editText.setHorizontallyScrolling(false)

         fabPrev  = view.findViewById(R.id.prev_btn);
         fabNext = view.findViewById(R.id.next_btn);

        fabNext.setOnClickListener({

            nextPage()
        })

        fabPrev.setOnClickListener({

            prevPage()
        })

        if(dataFile !=null && dataFile!!.listOfPageData.size==1){
            fabPrev.visibility  = View.GONE
            fabNext.visibility = View.GONE
        }

        if(editText!=null) {
            undoRedo = TextViewUndoRedo(editText,viewLifecycleOwner)
            editText!!.setHighlightWhileTextChanging(false)

        }
        return view
    }

    private fun prevPage() {

        if(currentPageIndex>0 && currentPageIndex < dataFile!!.listOfPageData.size && editText!=null){
            saveDataToPage()
            fabNext.isEnabled = true
            currentPageIndex--
            if(currentPageIndex==0){
                fabPrev.isEnabled = false
            }
                Toast.makeText(context, "page $currentPageIndex", Toast.LENGTH_SHORT).show()
            editText!!.setIsPrev(true)
            val hasChanges = hasUnsavedChanges.value
            editText!!.setText(dataFile!!.listOfPageData.get(currentPageIndex))
            hasUnsavedChanges.value = hasChanges
//                Log.e(TAG, "onCreateView: starting index $startingIndexOfCurrentPage")
//                editText.setStartIndex(startingIndexOfCurrentPage)
            //TODO : if not ....

            undoRedo = TextViewUndoRedo(editText,viewLifecycleOwner)

        }


    }

    private fun nextPage() {

        Log.e(TAG, "nextPage: ", )

        if(currentPageIndex>=0 && currentPageIndex < dataFile!!.listOfPageData.size-1 && editText!=null){
            fabPrev.isEnabled = true
            saveDataToPage()
            currentPageIndex++
            if(currentPageIndex==dataFile!!.listOfPageData.size-1){
                fabNext.isEnabled = false
            }
            editText!!.pushPrevPageStartLineIndex()
//            editText!!.prevPageStartLineIndex = editText!!.startIndex
                Toast.makeText(context, "page $currentPageIndex", Toast.LENGTH_SHORT).show()
            val startingIndexOfCurrentPage =/* editText!!.getStartingIndex()+*/ editText!!.findLineCount() + 1 ;
            editText!!.setStartIndex(startingIndexOfCurrentPage)
            val hasChanges = hasUnsavedChanges.value
            editText!!.setText(dataFile!!.listOfPageData.get(currentPageIndex))
            hasUnsavedChanges.value = hasChanges

            undoRedo = TextViewUndoRedo(editText,viewLifecycleOwner)

        }


    }
    fun undoChanges()
    {
        if(!undoRedo.canUndo){
            Toast.makeText(this.context, "no more Undo", Toast.LENGTH_SHORT).show()
        }
        var before = undoRedo.undo()
        while(before!=null && before.length>0 && before[0]!=' ' ) {
            before =  undoRedo.undo()
        }

        editText?.requestFocus()

    }
    fun redoChanges()
    {
        if(!undoRedo.canRedo){
            Toast.makeText(this.context, "no more Redo", Toast.LENGTH_SHORT).show()
        }
        var after=undoRedo.redo()
        while(after!=null && after.length>0 && after.last()!=' ' ) {
            after =  undoRedo.redo()
        }

        editText?.requestFocus()

    }


    fun saveDataToPage() {
        if(editText!=null) {


            val page = StringBuilder(editText!!.text.toString())
            Log.e(TAG, "saveDataToPage: page number $currentPageIndex", )
            if (dataFile != null && dataFile!!.listOfPageData.size>0 && currentPageIndex<dataFile!!.listOfPageData.size) {
                dataFile!!.listOfPageData.removeAt(currentPageIndex)
                dataFile!!.listOfPageData.add(currentPageIndex, page)
            }
        }
    }

    fun getFileName() : String{
        return dataFile!!.fileName
    }
    fun getFilePath() : String ?{
        if(dataFile!=null) return  dataFile!!.filePath
        return null
    }


    fun getUri(): Uri ?{
        if(dataFile!==null && dataFile!!.uri!==null)
            return dataFile!!.uri
        return null
    }

    fun getFileExtension() : String {
        if(dataFile!=null)
            return dataFile!!.fileExtension
        return ""
    }

    fun invalidateEditText()
    {
        if(editText!=null)
        {
            saveDataToPage()
            editText!!.setText(dataFile!!.listOfPageData[currentPageIndex])
        }
    }

    fun replaceAll(findText : String,replaceText : String,ignoreCase : Boolean = false){
        if(editText!=null) {
            val editTextData = editText!!.text.toString()
            val replacedData: String = editTextData.replace(findText, replaceText, ignoreCase)
            editText!!.setText(replacedData)
        }
    }

    fun highlight(find: String, index: Int,ignoreCase: Boolean): Int {

        val str: String = editText!!.text.toString()
        val sIndex: Int = str.indexOf(find, index, ignoreCase)

        if (sIndex != -1) {
            editText!!.requestFocus()
            editText!!.setSelection(sIndex, sIndex + find.length)
//            editText.setSelection(sIndex)
        }
        return sIndex
    }


    fun findReplace(find: String, replace: String, index: Int, ignoreCase: Boolean): Int {

        val string: String = editText!!.text.toString()
        if (index >= 0 && index < string.length) {
            val firstIndex: Int = string.indexOf(find, index, ignoreCase)
            if (firstIndex != -1) {
                val str2 = string.replaceRange(firstIndex, firstIndex + find.length, replace)
                editText!!.setText(str2)

            }
            return firstIndex;
        }
        return -1
    }

    fun gotoLine(line : Int){
        if (line <= 0) {
            editText?.setSelection(0)
        } else {
            val position = ordinalIndexOf(editText?.text.toString(), "\n", line)
            editText?.clearFocus()
            editText?.requestFocus()
            if (position != -1) {
                if(position!=0) editText!!.setSelection(position + 1)
                else editText?.setSelection(0)
            }
        }
    }

    fun ordinalIndexOf(str: String, substr: String?, line: Int): Int {
        var n = line
        if(line==1) return 0
        var pos = str.indexOf(substr!!)
        while (--n > 1 && pos != -1) pos = str.indexOf(substr, pos + 1)
        return pos
    }

    fun getTotalLine(): Int {
        return editText!!.lineCount
    }


    fun getListOfPages() : MutableList<StringBuilder>{
        return dataFile?.listOfPageData!!
    }


    fun insertSpecialChar(specialChar : String){
        if(editText!=null && editText!!.isFocused){
            editText!!.apply {
                    text?.replace(selectionStart,selectionEnd,specialChar)
            }
        }
    }
    fun selectionPrevPosition(){
        if(editText!=null && editText!!.isFocused){
            editText!!.apply {
                editText!!.setSelection(selectionEnd-1)
            }
        }

    }

    fun getSelectedData(): String? {
        if(editText!=null && editText!!.isFocused){
            editText?.apply {
                return text!!.substring(selectionStart,selectionEnd)
            }
        }
        return null
    }

    fun applySynatx(s: String) {



        Log.e(TAG, "applySynatx: $s", )
        if(editText!=null) {
            if (s == ".cpp") {
                mCurrentLanguage = Language.CPP
            } else if (s == ".c") {
                mCurrentLanguage = Language.C
            } else if (s == ".html") {
                mCurrentLanguage = Language.HTML
            } else if (s == ".py") {
                mCurrentLanguage = Language.PYTHON
            } else if (s == ".css") {
                mCurrentLanguage = Language.CSS
            } else if (s == ".js") {
                mCurrentLanguage = Language.JAVASCRIPT
            } else if (s == ".php") {
                mCurrentLanguage = Language.PHP
            } else if (s == ".kt") {
                mCurrentLanguage = Language.KOTLIN
            } else if (s == ".java") {
                mCurrentLanguage = Language.JAVA
            } else if (s == ".txt") {
                mCurrentLanguage = Language.TXT
            } else if (s == ".go") {
                mCurrentLanguage = Language.GO_LANG
            } else if (s == "default") {
                mCurrentLanguage = Language.DEFAULT
            } else if (s == ".xml") {
                mCurrentLanguage = Language.XML
            } else if (s == "no") {
                mCurrentLanguage = Language.NO_SYNTAX
            }
            configLanguageAutoComplete()
            SyntaxManager.applyMonokaiTheme(context, editText, mCurrentLanguage)
            invalidateEditText()

        }

    }

    fun setUri(muri: Uri) {
        if(dataFile!=null)
        {
            dataFile!!.uri = muri;
        }
    }


}