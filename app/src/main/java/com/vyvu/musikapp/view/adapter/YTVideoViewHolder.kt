package com.vyvu.musikapp.view.adapter

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.android.material.button.MaterialButton
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.R
import com.vyvu.musikapp.contract.uicontract.YTVideoViewHolderContract
import com.vyvu.musikapp.databinding.ItemYtVideoBinding
import com.vyvu.musikapp.model.Result
import com.vyvu.musikapp.presenter.YTViewHolderPresenter
import com.vyvu.musikapp.service.MusikService
import java.io.File
import kotlin.properties.Delegates

class YTVideoViewHolder(
    private val binding: ItemYtVideoBinding,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root), YTVideoViewHolderContract.View {
    private val presenter by lazy { YTViewHolderPresenter(this@YTVideoViewHolder) }
    private var result: Result? = null
    private var idDownloadMp3: Long? = null
    private var idDownloadThumb: Long? = null
    var urls = mutableListOf<String>()
    private var pos by Delegates.notNull<Int>()
    private val downloadManager by lazy { context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }

    fun bind(data: Result, position: Int) {
        result = data
        pos = position
        binding.run {

            ImageRequest.Builder(context)
                .data(data.video.thumbnailSrc)
                .crossfade(true)
                .target(imageThumbnail)
                .build()
                .run { presenter.requestLoadThumbnail(this, ImageLoader(context)) }
            textAuthor.text = data.uploader.username
            textTitle.text = data.video.title
            textDuration.text = data.video.duration
            buttonPlayOrPause.setOnClickListener {
                if (urls.isEmpty()) presenter.requestMp3URLs(data.video.id) {
                    handleClickStreamSong()
                } else handleClickStreamSong()
            }
            buttonDownload.setOnClickListener {
                if (urls.isEmpty()) presenter.requestMp3URLs(data.video.id) {
                    handleClickDownload()
                } else handleClickDownload()
            }
        }
    }

    private fun handleClickStreamSong() {
        val action = Intent(context, MusikService::class.java)
        binding.run {
            when (YTVideoAdapter.oldPos) {
                AppVals.Code.NONE_CODE -> {
                    streaming(buttonPlayOrPause, action)
                }
                pos -> stopping(buttonPlayOrPause, action)
                else -> {
                    YTVideoAdapter.oldButton?.setIconResource(R.drawable.ic_baseline_play_arrow_24)
                    streaming(buttonPlayOrPause, action)
                }
            }
        }
        context.startService(action)
    }

    private fun streaming(buttonPlayOrPause: MaterialButton, action: Intent) {
        YTVideoAdapter.oldButton = buttonPlayOrPause
        YTVideoAdapter.oldPos = pos
        YTVideoAdapter.currentPlayingButton = buttonPlayOrPause
        buttonPlayOrPause.setIconResource(R.drawable.ic_baseline_stop_circle_24)
        action.putExtra(AppVals.Action.INTENT_KEY_ACTIONS_CONTROL, AppVals.Action.PLAY_STREAM)
        action.putExtra(AppVals.Action.PLAY_STREAM, result!!.video.apply { mp3Src = urls.last() })
    }

    private fun stopping(buttonPlayOrPause: MaterialButton, action: Intent) {
        YTVideoAdapter.oldPos = AppVals.Code.NONE_CODE
        YTVideoAdapter.oldButton = null
        YTVideoAdapter.currentPlayingButton = null
        buttonPlayOrPause.setIconResource(R.drawable.ic_baseline_play_arrow_24)
        action.putExtra(AppVals.Action.INTENT_KEY_ACTIONS_CONTROL, AppVals.Action.STOP)
    }

    private fun handleClickDownload() {
        handleOnDownloadedAll()
        downloadMp3()
    }

    private fun downloadMp3() {
        result?.run {
            presenter.requestDownloadMp3(
                video.id + AppVals.String.EXTENSION_MP3,
                DownloadManager.Request(Uri.parse(urls.last()))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setTitle(video.title + AppVals.String.EXTENSION_MP3)
                    .setDescription(uploader.username),
                downloadManager
            )
        }
    }

    private fun downloadThumb() {
        result?.run {
            val thumbUrl = Uri.parse(video.thumbnailSrc)
            val thumbExtension = DOT + File(thumbUrl.path!!).extension
            presenter.requestDownloadThumbnail(
                video.id + thumbExtension,
                DownloadManager.Request(thumbUrl)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setTitle(File(thumbUrl.path!!).name + thumbExtension)
                    .setDescription(uploader.username),
                downloadManager
            )
        }
    }

    private fun handleOnDownloadedAll() {
        val broadcastReceiver = object : BroadcastReceiver() {
            private var isDownloadedMp3 = false
            override fun onReceive(context: Context?, intent: Intent?) {
                val downloadedId = intent?.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID,
                    AppVals.Code.NONE_CODE.toLong()
                )
                when {
                    downloadedId == idDownloadMp3 && !isDownloadedMp3 -> {
                        isDownloadedMp3 = true
                        downloadThumb()
                    }
                    isDownloadedMp3 -> {
                        binding.buttonDownload.isEnabled = true
                        this@YTVideoViewHolder.context.unregisterReceiver(this)
                    }
                }
            }
        }
        context.registerReceiver(
            broadcastReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    override fun onSendingDownloadMp3() {
        binding.buttonDownload.isEnabled = false
    }

    override fun onSentDownloadMp3(downloadId: Long) {
        idDownloadMp3 = downloadId
    }

    override fun onSentDownloadThumbnail(downloadId: Long) {
        idDownloadThumb = downloadId
    }

    override fun showProgress() {
        binding.run {
            buttonDownload.isEnabled = false
            buttonPlayOrPause.isEnabled = false
        }
    }

    override fun dismissProgress() {
        binding.run {
            buttonDownload.isEnabled = true
            buttonPlayOrPause.isEnabled = true
        }
    }

    override fun onDataReceived(data: MutableList<String>) {
        urls.addAll(data)
    }

    override fun onDataFailed(exception: Exception) {
        throw exception
    }

    private companion object {
        const val DOT = "."
    }
}
