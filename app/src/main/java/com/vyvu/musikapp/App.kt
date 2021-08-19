package com.vyvu.musikapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.vyvu.musikapp.repo.DataManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        DataManager.create()
        initNotifyChannel()
    }

    private fun initNotifyChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                getString(R.string.text_app_name),
                getString(R.string.text_app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(serviceChannel)
        }
    }
}
