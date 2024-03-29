package com.nbow.advanceeditor.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase




@Database(entities = arrayOf(History::class, RecentFile::class), version = 2 )
abstract class MyDatabase : RoomDatabase() {
    abstract fun HistoryDao(): HistoryDao
    abstract fun RecentFileDao(): RecentFileDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "code_editor"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}