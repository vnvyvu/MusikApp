package com.vyvu.musikapp.model

import android.os.Parcelable
import com.beust.klaxon.Json
import com.vyvu.musikapp.AppVals
import kotlinx.parcelize.Parcelize

@Parcelize
data class YTVideo(
    val results: List<Result>
) : Parcelable

@Parcelize
data class Result(
    val uploader: Uploader = Uploader(),
    val video: Video = Video()
) : Parcelable

@Parcelize
data class Uploader(
    val username: String = AppVals.String.EMPTY_STRING,
) : Parcelable

@Parcelize
class Video(
    val duration: String = AppVals.String.EMPTY_STRING,
    val id: String = AppVals.String.EMPTY_STRING,
    val snippet: String = AppVals.String.EMPTY_STRING,
    @Json(name = "thumbnail_src")
    val thumbnailSrc: String = AppVals.String.EMPTY_STRING,
    val title: String = AppVals.String.EMPTY_STRING,
    @Json(name = "upload_date")
    val uploadDate: String = AppVals.String.EMPTY_STRING,
    val url: String = AppVals.String.EMPTY_STRING,
    val views: String = AppVals.String.EMPTY_STRING,
    var mp3Src: String = AppVals.String.EMPTY_STRING
) : Parcelable
