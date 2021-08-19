package com.vyvu.musikapp.presenter

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import coil.ImageLoader
import coil.request.ImageRequest
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.contract.uicontract.YTVideoViewHolderContract
import com.vyvu.musikapp.repo.Mp3Repository
import com.vyvu.musikapp.repo.YTVideoRepository
import kotlinx.coroutines.*
import java.io.File

class YTViewHolderPresenter(private var view: YTVideoViewHolderContract.View) :
    YTVideoViewHolderContract.Presenter {
    private val repositoryYTVideo: YTVideoRepository = YTVideoRepository.instance
    private val repositoryMp3: Mp3Repository = Mp3Repository.instance

    override fun requestDownloadMp3(
        fileName: String,
        request: DownloadManager.Request,
        downloadManager: DownloadManager
    ): Uri =
        Uri.fromFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).absolutePath + AppVals.String.SLASH_STRING + fileName))
            .also {
                view.run {
                    onSendingDownloadMp3()
                    onSentDownloadMp3(
                        repositoryMp3.downloadMedia(
                            request.apply { setDestinationUri(it) },
                            downloadManager
                        )
                    )
                }
            }

    override fun requestDownloadThumbnail(
        fileName: String,
        request: DownloadManager.Request,
        downloadManager: DownloadManager
    ): Uri =
        Uri.fromFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + AppVals.String.SLASH_STRING + fileName))
            .also {
                view.run {
                    onSentDownloadThumbnail(
                        repositoryMp3.downloadMedia(
                            request.apply { setDestinationUri(it) },
                            downloadManager
                        )
                    )
                }
            }

    override fun requestMp3URLs(id: String, callback: () -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            view.run {
                showProgress()
                try {
                    onDataReceived(withContext(Dispatchers.IO) {
                        return@withContext repositoryYTVideo.getMp3URLs(id)
                    })
                    dismissProgress()
                    callback.invoke()
                } catch (e: Exception) {
                    onDataFailed(e)
                }
            }
        }
    }

    override fun requestLoadThumbnail(imageRequest: ImageRequest, loader: ImageLoader) {
        loader.enqueue(imageRequest)
    }
}
