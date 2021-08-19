package com.vyvu.musikapp.contract.modelcontract

import com.vyvu.musikapp.contract.BaseContract
import com.vyvu.musikapp.model.YTVideo

interface YTVideoContract {
    fun getYTVideo(keyword: String, page: Int): YTVideo?
    fun getMp3URLs(videoId: String): MutableList<String>
}
