package de.fra_uas.fb2.mobiledevices.visualizetilt.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_data")
data class MyDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val x: Float,
    val y: Float,
    val z: Float,
    val image: ByteArray
)
