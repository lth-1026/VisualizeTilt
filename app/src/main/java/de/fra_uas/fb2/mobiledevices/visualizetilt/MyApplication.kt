package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.app.Application
import androidx.room.Room

class MyApplication : Application() {
    lateinit var database: MyDatabase

    override fun onCreate() {
        super.onCreate()
        // Room 데이터베이스 인스턴스를 생성 및 초기화
        database = Room.databaseBuilder(
            applicationContext,
            MyDatabase::class.java, "my-database"
        ).build()
    }
}