package com.nbow.advanceeditor.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.sql.Date


@Entity(indices = [Index(value = ["uriString"], unique = true)],tableName = "recentfile")
data class RecentFile(

    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val uriString: String,
    val fileName:String,
    val date:String,
    val fileSize:Int
)
