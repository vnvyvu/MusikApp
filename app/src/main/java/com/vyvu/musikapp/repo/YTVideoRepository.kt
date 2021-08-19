package com.vyvu.musikapp.repo

import com.vyvu.musikapp.contract.modelcontract.YTVideoContract
import com.vyvu.musikapp.model.YTVideo

class YTVideoRepository(private val ytVideoContract: YTVideoContract?) : YTVideoContract {
    init { instance = this }

    override fun getYTVideo(keyword: String, page: Int): YTVideo? =
        ytVideoContract!!.getYTVideo(keyword, page)

    override fun getMp3URLs(videoId: String): MutableList<String> =
        ytVideoContract!!.getMp3URLs(videoId)

    companion object {
        lateinit var instance: YTVideoRepository
            private set
    }
}
