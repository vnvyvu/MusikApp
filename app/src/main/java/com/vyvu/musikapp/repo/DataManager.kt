package com.vyvu.musikapp.repo

import com.vyvu.musikapp.contract.modelcontract.impl.Mp3Impl
import com.vyvu.musikapp.contract.modelcontract.impl.YTVideoImpl

class DataManager {
    companion object {
        fun create() {
            Mp3Repository(Mp3Impl())
            YTVideoRepository(YTVideoImpl())
        }
    }
}
