package com.vyvu.musikapp.contract.modelcontract.impl

import com.beust.klaxon.Klaxon
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.contract.modelcontract.YTVideoContract
import com.vyvu.musikapp.model.YTVideo
import com.vyvu.musikapp.utils.HTTPRequestUtils

class YTVideoImpl : YTVideoContract {
    override fun getYTVideo(keyword: String, page: Int): YTVideo? =
        Klaxon().parse<YTVideo>(
            HTTPRequestUtils.getBody(
                BASE_YOUTUBE_URL
                    .replaceFirst(PAGE_NUMBER, page.toString())
                    .replaceFirst(QUERY_STRING_PLACEHOLDER, keyword)
            )
        )

    override fun getMp3URLs(videoId: String): MutableList<String> =
        HTTPRequestUtils.getBody(BASE_SERVER_URL + videoId).run {
            val titleData = HTTPRequestUtils.RequestBodyUtils.getAttValue(
                DIV_TAG,
                TITLE_ATTRIBUTE,
                this
            ).first()
            HTTPRequestUtils.RequestBodyUtils.getAttValue(
                LI_TAG,
                TOKEN_ATTRIBUTE,
                this
            )
                .map { BASE_SERVER_URI + AppVals.String.SLASH_STRING + it + AppVals.String.SLASH_STRING + titleData + AppVals.String.EXTENSION_MP3 }
                .toMutableList()
        }

    private companion object {
        const val PAGE_NUMBER = "{page}"
        const val QUERY_STRING_PLACEHOLDER = "{q}"
        const val BASE_YOUTUBE_URL =
            "http://yt-scrape.hanet.com/api/search?page=$PAGE_NUMBER&q=$QUERY_STRING_PLACEHOLDER"

        const val BASE_SERVER_URL = "https://api.vevioz.com/@api/json/mp3/"
        const val BASE_SERVER_URI = "https://api.vevioz.com/download/"
        const val TOKEN_ATTRIBUTE = "data-mp3-tag"
        const val LI_TAG = "li"
        const val TITLE_ATTRIBUTE = "data-yt-title"
        const val DIV_TAG = "div"
    }
}
