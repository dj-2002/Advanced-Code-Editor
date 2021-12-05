package com.nbow.advanceeditor

import android.net.Uri

data class DataFile (
    val fileName : String,
    val filePath : String,
    val uri : Uri,
    val listOfPageData : MutableList<StringBuilder> = arrayListOf()
    ){
    val fileExtension = when(val index = fileName.lastIndexOf(".")){
        -1 -> String()
        else -> fileName.substring(index)
    }

}