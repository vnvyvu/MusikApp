package com.vyvu.musikapp.presenter

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.contract.uicontract.MainActivityContract
import com.vyvu.musikapp.model.Mp3
import com.vyvu.musikapp.repo.Mp3Repository
import com.vyvu.musikapp.repo.YTVideoRepository
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class MainActivityPresenter(private var view: MainActivityContract.View) :
    MainActivityContract.Presenter {
    private val repositoryYTVideo: YTVideoRepository = YTVideoRepository.instance
    private val repositoryMp3: Mp3Repository = Mp3Repository.instance

    override fun requestYTVideo(keyword: String, page: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            view.run {
                showProgress()
                try {
                    onDataReceived(repositoryYTVideo.run {
                        withContext(Dispatchers.IO) {
                            return@withContext getYTVideo(keyword, page)
                        }
                    }!!.results.filter { it.video.id != AppVals.String.EMPTY_STRING }
                        .toMutableList())
                } catch (e: Exception) {
                    onDataFailed(e)
                } finally {
                    dismissProgress()
                }
            }
        }
    }

    override fun requestMp3(
        selection: String,
        selectionArgs: Array<String>,
        contentResolver: ContentResolver
    ) {
        view.run {
            showProgress()
            try {
                onDataReceived(
                    (repositoryMp3.getStorageMp3(
                        selection,
                        selectionArgs,
                        contentResolver
                    )?: mutableListOf<Mp3>())
                )
            } catch (e: Exception) {
                onDataFailed(e)
            } finally {
                dismissProgress()
            }
        }
    }

    override fun requestAllMp3(contentResolver: ContentResolver) {
        view.run {
            showProgress()
            try {
                onDataReceived(
                    repositoryMp3.getStorageMp3(
                        SELECTION_BY_DURATION,
                        arrayOf(
                            TimeUnit.SECONDS.convert(
                                DEFAULT_MIN_DURATION,
                                TimeUnit.SECONDS
                            ).toString()
                        ),
                        contentResolver
                    )!!.toMutableList()
                )
            } catch (e: Exception) {
                onDataFailed(e)
            } finally {
                dismissProgress()
            }
        }
    }

    override fun requestRegisterBroadcast(
        activity: Activity,
        filter: IntentFilter
    ): BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                try {
                    view.onReceivedBroadcast(intent!!)
                } catch (e: Exception) {
                    view.onDataFailed(e)
                }
            }
        }.apply {
            LocalBroadcastManager.getInstance(activity).registerReceiver(this, filter)
        }

    private companion object {
        const val SELECTION_BY_DURATION = "${MediaStore.Audio.Media.DURATION} >= ?"
        const val DEFAULT_MIN_DURATION: Long = 60000
    }
}
