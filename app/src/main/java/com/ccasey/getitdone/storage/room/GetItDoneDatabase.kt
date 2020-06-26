package com.ccasey.getitdone.storage.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ccasey.getitdone.model.Run

@Database(entities = [Run::class], version = 1)
abstract class GetItDoneDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao

    companion object {

    }
}