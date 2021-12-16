package com.nbow.advanceeditor

import android.app.Application
import android.content.Context
import android.net.Uri
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import com.nbow.advanceeditor.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import java.lang.StringBuilder
import java.util.*

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val fragmentList = MutableLiveData<MutableList<EditorFragment>>(arrayListOf())

    private val repository : Repository = Repository(application)
    private  var recentFileList = MutableLiveData(mutableListOf<RecentFile>())

//    var currentTab : Int = -1
    var currentTabIndex = -1
    var isWrap = false
    var isHistoryLoaded = MutableLiveData(false)

    private val TAG = "MyViewModel"

    init {
        loadHistory(application.applicationContext)
        loadRecentFile()
        val preferences = PreferenceManager.getDefaultSharedPreferences(application)
        isWrap = preferences.getBoolean("word_wrap",true)

    }

    fun getRecentFileList(): LiveData<MutableList<RecentFile>> {
        return recentFileList
    }

    private fun loadRecentFile() {
        viewModelScope.launch(Dispatchers.IO) {
            recentFileList.postValue(repository.getRecentFileList())
            Log.e(TAG,"recent file list size ${recentFileList.value!!.size}")

        }
    }

    fun addHistories(context: Context){

        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllHistory(context)
            for(frag : Fragment in fragmentList.value!!){
                val editorFragment = frag as EditorFragment
                val uniqueFileName = editorFragment.getFileName() + (0..10000).random()
                val file = File(context.filesDir, uniqueFileName)
                if (!file.exists()) file.createNewFile()
                context.openFileOutput(uniqueFileName, Context.MODE_PRIVATE).use {
                    it.write(
                        editorFragment.getEditTextData().toString().toByteArray())
                }

                val uriString=editorFragment.getUri().toString()
                val history = History(
                    0,
                   uriString= uriString,
                    fileName = uniqueFileName,
                    realFileName = editorFragment.getFileName(),
                    hasUnsavedData = editorFragment.hasUnsavedChanges.value ?: true,
                )
                Log.e(TAG, "saving new file to databse: file id ${history.historyId}")
                repository.addHistory(history)
            }

        }
    }


    fun loadHistory( context: Context){

        viewModelScope.launch(Dispatchers.IO) {

            val historyList:MutableList<History> = repository.getHistory()
            Log.e("view model","size history ${historyList.size} fragment list size : ${fragmentList.value!!.size}")

            for(history in historyList) {
                val uri: Uri = Uri.parse(history.uriString)
                val data = StringBuilder()
                val mlist:MutableList<StringBuilder> = arrayListOf()
                var temp = StringBuilder("")
                var count = 0


                context.openFileInput(history.fileName).bufferedReader().forEachLine { line ->
                    temp.append(line + "\n")
                    count++
                    if (count >= 500 || temp.length >= 500000) { // 5kb
                        mlist.add(temp)
                        Log.e(TAG, "loadHistory: making another page prev page lines $count and size is ${temp.length}", )
                        temp = StringBuilder()
                        count = 0
                    }
                }
                if (temp.isNotEmpty()) {
                    mlist.add(temp)
                }
                Log.e(TAG, "loadHistory: total pages ${mlist.size}", )
                val datafile = DataFile(
                    history.realFileName,
                    uri.path!!,
                    uri,mlist)
                val frag = EditorFragment(datafile,  history.hasUnsavedData)
                Log.e(TAG, "loadHistory: hasUnsavedData : ${history.hasUnsavedData}")
                (fragmentList.value ?: arrayListOf()).add(frag)
            }
            isHistoryLoaded.postValue(true)

        }

    }



    fun getFragmentList(): LiveData<MutableList<EditorFragment>> {
        return fragmentList
    }

    fun setFragmentList(fragmentList : MutableList<EditorFragment>){
        this.fragmentList.value = fragmentList
    }

    fun addRecentFile(recentFile: RecentFile)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveToRecentFile(recentFile)
        }
    }

    fun deleteRecentFile(recentFile: RecentFile)
    {
        viewModelScope.launch(IO){
            repository.deleteRecentFile(recentFile)
        }
    }

    fun deleteAllRecentFile() {
        viewModelScope.launch(IO) {
            repository.deleteAllRecentFile()
        }
    }


}