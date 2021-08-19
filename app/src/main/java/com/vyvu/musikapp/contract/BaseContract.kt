package com.vyvu.musikapp.contract

import android.content.Context
import android.content.Intent

interface BaseContract {
    interface View<T> {
        fun showProgress()
        fun dismissProgress()
        fun onDataReceived(data: T)
        fun onDataFailed(exception: Exception) {
            throw exception
        }
    }
}
