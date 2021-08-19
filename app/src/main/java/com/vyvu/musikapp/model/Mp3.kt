package com.vyvu.musikapp.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Mp3(
    val id: Long,
    val uri: Uri,
    var thumbnailUri: Uri?,
    val album: String,
    val title: String,
    val artist: String,
    val duration: Long,
) : Parcelable
