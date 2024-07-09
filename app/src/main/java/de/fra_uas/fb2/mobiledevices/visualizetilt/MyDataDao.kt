package de.fra_uas.fb2.mobiledevices.visualizetilt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MyDataDao {
    @Query("SELECT * FROM my_data")
    fun getAll(): List<MyDataEntity>

    @Query("DELETE FROM my_data WHERE id = :id")
    fun delete(id: Int)

    @Insert
    suspend fun insert(data: MyDataEntity)
}