package com.vyvu.musikapp.service.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import coil.ImageLoader
import coil.request.ImageRequest
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.R
import com.vyvu.musikapp.model.Mp3
import com.vyvu.musikapp.model.Video
import com.vyvu.musikapp.service.MusikService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusikUtils {
    companion object {
        fun getBaseNotificationPlaylist(
            service: MusikService,
            mp3Data: Mp3?,
            style: Notification.MediaStyle,
            contentResolver: ContentResolver
        ): Notification.Builder? {
            return mp3Data?.run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notify = Notification.Builder(service, AppVals.String.APP_NAME)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setContentTitle(title)
                        .setContentText(artist)
                        .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                        .setStyle(style)
                    if (thumbnailUri == null) notify.setLargeIcon(
                        Icon.createWithResource(
                            service,
                            R.drawable.default_large_icon
                        )
                    )
                    else notify.setLargeIcon(getBitmap(contentResolver, thumbnailUri!!))
                    notify
                } else null
            }
        }

        suspend fun getBaseNotificationStream(
            service: MusikService,
            streamData: Video?,
            style: Notification.MediaStyle
        ): Notification.Builder? =
            withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    streamData?.run {
                        Notification.Builder(service, AppVals.String.APP_NAME)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setContentTitle(title)
                            .setContentText(url)
                            .setLargeIcon(getBitmap(service, thumbnailSrc))
                            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                            .setStyle(style)
                    }
                } else null
            }

        @SuppressLint("UnspecifiedImmutableFlag")
        fun buildActionButton(service: MusikService, resId: Int, actionName: String, reqCode: Int) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                Notification.Action.Builder(
                    Icon.createWithResource(service, resId),
                    actionName,
                    PendingIntent.getService(
                        service,
                        reqCode,
                        Intent(service, MusikService::class.java).apply {
                            putExtra(AppVals.Action.INTENT_KEY_ACTIONS_CONTROL, actionName)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            else
                Notification.Action.Builder(
                    resId, actionName, PendingIntent.getService(
                        service,
                        reqCode,
                        Intent(service, MusikService::class.java).apply {
                            putExtra(AppVals.Action.INTENT_KEY_ACTIONS_CONTROL, actionName)
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )

        private fun getBitmap(contentResolver: ContentResolver, uri: Uri) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
            else
                MediaStore.Images.Media.getBitmap(contentResolver, uri)

        private suspend fun getBitmap(service: MusikService, url: String) =
            withContext(Dispatchers.IO) {
                return@withContext (ImageLoader(service).execute(
                    ImageRequest.Builder(service)
                        .data(url)
                        .build()
                ).drawable as BitmapDrawable).bitmap
            }
    }
}
