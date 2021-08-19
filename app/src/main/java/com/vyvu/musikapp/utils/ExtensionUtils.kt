package com.vyvu.musikapp.utils


class ExtensionUtils {
    companion object {
        const val SEC_TO_MINUTE = 60
        const val SEC_TO_MILISEC = 1000
        const val TEN = 10
        const val ZERO_STRING = "0"
        const val COLON = ":"
    }
}

internal fun Long.toTime(): String {
    var ss = this / ExtensionUtils.SEC_TO_MILISEC;
    val mm = ss / ExtensionUtils.SEC_TO_MINUTE
    ss -= mm * ExtensionUtils.SEC_TO_MINUTE
    return "$mm${ExtensionUtils.COLON}${if (ss < ExtensionUtils.TEN) ExtensionUtils.ZERO_STRING + ss else ss}"
}

internal fun String.toSecond(): Long = this.split(ExtensionUtils.COLON).run {
    (this.last().toLong() +
            this.first().toLong() * ExtensionUtils.SEC_TO_MINUTE)
}
