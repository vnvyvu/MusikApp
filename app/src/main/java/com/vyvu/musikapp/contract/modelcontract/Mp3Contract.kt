package com.vyvu.musikapp.contract.modelcontract

import android.app.DownloadManager
import android.content.ContentResolver
import com.vyvu.musikapp.contract.BaseContract
import com.vyvu.musikapp.model.Mp3

interface Mp3Contract{
    fun downloadMedia(request: DownloadManager.Request, downloadManager: DownloadManager): Long
    fun getStorageMp3(
        selection: String,
        selectionArgs: Array<String>,
        contentResolver: ContentResolver
    ): MutableList<Mp3>?
}
