

package com.nbow.advanceeditor.data
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


import androidx.room.Index

@Entity(indices = [Index(value = ["uriString"], unique = true)] , tableName = "history")
data class History(
        @PrimaryKey(autoGenerate = true)
        val historyId: Long,             //file id
        val fileName: String,
        val uriString: String,
        val realFileName:String,
        val hasUnsavedData : Boolean,
)

