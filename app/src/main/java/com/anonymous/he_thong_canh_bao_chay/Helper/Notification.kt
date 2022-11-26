package com.anonymous.he_thong_canh_bao_chay.Helper

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.anonymous.he_thong_canh_bao_chay.MyApplication.MyApplication
import com.anonymous.he_thong_canh_bao_chay.R

object Notification {

    fun sendNotification(title : String, content : String, mContext : Context) {
        val notification = NotificationCompat.Builder(mContext,MyApplication.ID)
                            .setSmallIcon(R.drawable.ic_baseline_warning_24)
                            .setContentText(content)
                            .setContentTitle(title)
            .build()
        //val notificationCompat = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationCompat = NotificationManagerCompat.from(mContext)
        notificationCompat.notify(1, notification)
    }
}