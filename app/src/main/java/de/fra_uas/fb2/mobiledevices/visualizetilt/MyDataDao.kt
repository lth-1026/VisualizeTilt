package de.fra_uas.fb2.mobiledevices.visualizetilt

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface MyDataDao {
    @Insert
    suspend fun insert(data: MyDataEntity)
}