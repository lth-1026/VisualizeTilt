package de.fra_uas.fb2.mobiledevices.visualizetilt.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MyDataEntity::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract fun myDataDao(): MyDataDao
}