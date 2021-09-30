package com.nbow.advanceeditor

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.*

class EditorFragment : Fragment {

    private val TAG = "EditorFragment"
    private var editText : MyEditText? = null
    private var currentPageIndex : Int = 0
    var hasUnsavedChanges = MutableLiveData(false)
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
            for(page in dataFile!!.listOfPageData){
                temp.append("$page\n")
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
            Log.e(TAG, "onViewStateRestored: size : ${dataFile!!.listOfPageData.get(0).length}")
            Log.e(TAG, "onViewStateRestored: number of page : ${dataFile!!.listOfPageData.size}")


        }
        Log.e(TAG, "onViewStateRestored: current index of page : $currentPageIndex")


        super.onViewStateRestored(savedInstanceState)
    }


    override fun onResume() {
//        val PREFERENCE_NAME="myPreference"
        val KEY_TEXT_SIZE = "TEXT_SIZE_PREFERENCE"
        val preference= PreferenceManager.getDefaultSharedPreferences(context)
        var myTextSize:Int = preference.getInt(KEY_TEXT_SIZE,14)

        editText?.setTextSize(myTextSize.toFloat())


        val  f=(preference.getString("font_family","DEFAULT"))
        if(f=="DEFAULT_BOLD")
            editText?.typeface= Typeface.DEFAULT_BOLD
        else if(f=="MONOSPACE")
            editText?.typeface= Typeface.MONOSPACE
        else if(f=="SANS_SARIF")
            editText?.typeface= Typeface.SANS_SERIF
        else if(f=="SERIF")
            editText?.typeface= Typeface.SERIF


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

        if(!isWrap) layout = R.layout.fragment_editor_unwrap

        val view = inflater.inflate(layout, container, false)

//        createPagesFromListOfLines()

        currentPageIndex = 0
        editText = view.findViewById(R.id.editText)
        editText!!.setLifeCycleOwner(viewLifecycleOwner)
        if(dataFile!=null && editText!=null){
            editText!!.extension = dataFile!!.fileExtension
            editText!!.setHashMapValue()
        }
        editText!!.hasUnsavedChanges.value = hasUnsavedChanges.value
        editText!!.hasUnsavedChanges.observe(viewLifecycleOwner){
            hasUnsavedChanges.value = it
//            Log.e(TAG, "onCreateView: observe called value of unsaved change $it ${hasUnsavedChanges.value}")
        }

//        editText?.inputType =(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//        editText?.isSingleLine = false

        editText?.setStartIndex(1)
        val prev = view.findViewById<Button>(R.id.prev)
        val next = view.findViewById<Button>(R.id.next)

        if(dataFile !=null && dataFile!!.listOfPageData.size==1){
            prev.visibility  = View.GONE
            next.visibility = View.GONE
        }
        prev.setOnClickListener(View.OnClickListener {
//            Toast.makeText(context, "prev clicked", Toast.LENGTH_SHORT).show()
            if(currentPageIndex>0 && currentPageIndex < dataFile!!.listOfPageData.size && editText!=null){
                saveDataToPage()
                next.isEnabled = true
                currentPageIndex--
                if(currentPageIndex==0){
                    prev.isEnabled = false
                }
//                Toast.makeText(context, "page $currentPageIndex", Toast.LENGTH_SHORT).show()
                editText!!.setIsPrev(true)
                editText!!.setText(dataFile!!.listOfPageData.get(currentPageIndex))
//                Log.e(TAG, "onCreateView: starting index $startingIndexOfCurrentPage")
//                editText.setStartIndex(startingIndexOfCurrentPage)
                //TODO : if not ....
            }
        })
        next.setOnClickListener(View.OnClickListener {
            if(currentPageIndex>=0 && currentPageIndex < dataFile!!.listOfPageData.size-1 && editText!=null){
                prev.isEnabled = true
                saveDataToPage()
                currentPageIndex++
                if(currentPageIndex==dataFile!!.listOfPageData.size-1){
                    next.isEnabled = false
                }
//                Toast.makeText(context, "page $currentPageIndex", Toast.LENGTH_SHORT).show()
                val startingIndexOfCurrentPage = editText!!.getStartingIndex()+editText!!.lineCount
                editText!!.setStartIndex(startingIndexOfCurrentPage)
                editText!!.setText(dataFile!!.listOfPageData.get(currentPageIndex))
            }
        })

//        editText.setHorizontallyScrolling(false)
        if(editText!=null) {
            undoRedo = TextViewUndoRedo(editText,viewLifecycleOwner)
        }
        return view
    }

    fun undoChanges()
    {
        var before = undoRedo.undo()
        while(before!=null && before.length>0 && before[0]!=' ' ) {
            before =  undoRedo.undo()
        }

    }
    fun redoChanges()
    {
        var after=undoRedo.redo()
        while(after!=null && after.length>0 && after.last()!=' ' ) {
            after =  undoRedo.redo()
        }
    }


    fun saveDataToPage() {
        if(editText!=null) {
            val page = editText!!.text.toString()
            if (dataFile != null) {
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
    fun getStartingIndexOfEdittext():Int{
        return editText!!.getStartingIndex()
    }

    fun getListOfPages() : MutableList<String>{
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


}