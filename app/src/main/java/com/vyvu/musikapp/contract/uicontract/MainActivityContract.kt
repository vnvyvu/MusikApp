package com.vyvu.musikapp.contract.uicontract

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.result.ActivityResultLauncher
import com.vyvu.musikapp.contract.BaseContract

interface MainActivityContract {
    interface Presenter {
        fun requestYTVideo(keyword: String, page: Int)
        fun requestMp3(
            selection: String,
            selectionArgs: Array<String>,
            contentResolver: ContentResolver
        )
        fun requestAllMp3(contentResolver: ContentResolver)
        fun requestRegisterBroadcast(activity: Activity, filter: IntentFilter): BroadcastReceiver
    }

    interface View : BaseContract.View<MutableList<*>> {
        fun onReceivedBroadcast(dataReceived: Intent)
    }
}
