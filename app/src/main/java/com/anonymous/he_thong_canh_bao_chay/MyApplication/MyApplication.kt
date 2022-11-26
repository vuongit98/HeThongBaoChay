package com.anonymous.he_thong_canh_bao_chay.MyApplication

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.anonymous.he_thong_canh_bao_chay.Helper.MyShareReference

class MyApplication : Application() {
    companion object{
        const val ID = "CHANNEL"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        MyShareReference.init(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val name : CharSequence = "notification test"
        val channel = NotificationChannel(ID, name,NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "notification description"

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}