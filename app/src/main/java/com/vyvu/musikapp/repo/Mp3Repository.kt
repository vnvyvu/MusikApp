package com.vyvu.musikapp.repo

import android.app.DownloadManager
import android.content.ContentResolver
import android.util.Log
import com.vyvu.musikapp.contract.modelcontract.Mp3Contract
import com.vyvu.musikapp.model.Mp3


class Mp3Repository(private val mp3Contract: Mp3Contract) : Mp3Contract {
    init { instance = this }

    override fun downloadMedia(
        request: DownloadManager.Request,
        downloadManager: DownloadManager
    ): Long = mp3Contract.downloadMedia(request, downloadManager)

    override fun getStorageMp3(
        selection: String,
        selectionArgs: Array<String>,
        contentResolver: ContentResolver
    ): MutableList<Mp3>? = mp3Contract.getStorageMp3(selection, selectionArgs, contentResolver)

    companion object {
        lateinit var instance: Mp3Repository
            private set
    }
}
