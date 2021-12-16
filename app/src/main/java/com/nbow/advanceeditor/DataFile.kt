package com.nbow.advanceeditor

import android.net.Uri
import java.util.regex.Pattern

data class DataFile (
    val fileName : String,
    val filePath : String,
    var uri : Uri,
    val listOfPageData : MutableList<StringBuilder> = arrayListOf()
    )
    {
    val fileExtension = when(val index = fileName.lastIndexOf(".")){
        -1 -> String()
        else ->{
            val PATTERN = Pattern.compile("[\\s]*[(]\\b(\\d*[.]?\\d+)\\b[)]")
            val matcher = PATTERN.matcher(fileName)
            if(matcher.find()){
                fileName.substring(index,matcher.start())
            }else
                fileName.substring(index)
           // getExt(index)
        }
    }



}