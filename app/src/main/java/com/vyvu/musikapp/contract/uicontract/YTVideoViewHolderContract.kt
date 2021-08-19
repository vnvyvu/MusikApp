package com.vyvu.musikapp.contract.uicontract

import android.app.DownloadManager
import android.net.Uri
import coil.ImageLoader
import coil.request.ImageRequest
import com.vyvu.musikapp.contract.BaseContract

interface YTVideoViewHolderContract {
    interface Presenter {
        fun requestMp3URLs(id: String, callback: () -> Unit)
        fun requestLoadThumbnail(imageRequest: ImageRequest, loader: ImageLoader)
        fun requestDownloadMp3(
            fileName: String,
            request: DownloadManager.Request,
            downloadManager: DownloadManager
        ): Uri

        fun requestDownloadThumbnail(
            fileName: String,
            request: DownloadManager.Request,
            downloadManager: DownloadManager
        ): Uri
    }

    interface View : BaseContract.View<MutableList<String>> {
        fun onSendingDownloadMp3()
        fun onSentDownloadMp3(downloadId: Long)
        fun onSentDownloadThumbnail(downloadId: Long)
    }
}
