package com.vyvu.musikapp.contract.modelcontract.impl

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.contract.modelcontract.Mp3Contract
import com.vyvu.musikapp.model.Mp3

class Mp3Impl : Mp3Contract {
    override fun downloadMedia(
        request: DownloadManager.Request,
        downloadManager: DownloadManager
    ) = downloadManager.enqueue(request)

    override fun getStorageMp3(
        selection: String,
        selectionArgs: Array<String>,
        contentResolver: ContentResolver
    ): MutableList<Mp3>? =
        contentResolver.query(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DISPLAY_NAME
            ), selection, selectionArgs, null
        )?.run {
            val mp3s = mutableListOf<Mp3>()
            while (moveToNext()) {
                val displayName =
                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)).removePrefix(
                        APP_TYPE
                    ).removeSuffix(AppVals.String.EXTENSION_MP3)
                val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val uri =
                    ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                val album = handleBadSrc(
                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
                    uri,
                    contentResolver
                )
                mp3s += Mp3(
                    id, uri,
                    getThumb(displayName, contentResolver),
                    album,
                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
                    getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                )
            }
            close()
            mp3s
        }

    private fun getThumb(displayName: String, contentResolver: ContentResolver): Uri? =
        contentResolver.query(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Images.Media._ID,
            ),
            SELECTION_BY_DISPLAY_NAME,
            arrayOf("${PERCENT}$displayName${PERCENT}"),
            null
        )?.run {
            var thumbUri: Uri? = null
            if (moveToNext()) {
                thumbUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    getLong(getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                )
            }
            close()
            thumbUri
        }

    private fun handleBadSrc(album: String, uri: Uri, contentResolver: ContentResolver) =
        AppVals.String.MUSIK_SERVICE_NAME.apply {
            if (BAD_SRC in album) {
                contentResolver.update(
                    uri,
                    ContentValues().apply {
                        put(
                            MediaStore.Audio.Media.ALBUM,
                            AppVals.String.MUSIK_SERVICE_NAME
                        )
                    },
                    null, null
                )
            }
        }

    private companion object {
        const val BAD_SRC = "api.vevioz.com"
        const val APP_TYPE = "Music"
        const val PERCENT = "%"
        const val SELECTION_BY_DISPLAY_NAME = "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"
    }
}
