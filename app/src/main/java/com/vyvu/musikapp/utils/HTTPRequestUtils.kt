package com.vyvu.musikapp.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class HTTPRequestUtils {
    class RequestBodyUtils {
        companion object {
            fun getAttValue(
                nameOfTag: String,
                nameOfAtt: String,
                html: String
            ): MutableList<String> {
                val values = mutableListOf<String>()
                val m = Pattern.compile("<$nameOfTag[^>]+$nameOfAtt=['\"]([^'\"]+)['\"][^>]*>")
                    .matcher(html)
                while (m.find()) {
                    values.add(m.group(1)!!)
                }
                return values
            }
        }
    }

    companion object {
        fun getBody(url: String): String {
            val connection = URL(url).openConnection() as HttpURLConnection
            val res = StringBuffer()
            try {
                val input = BufferedReader(InputStreamReader(connection.inputStream))
                var line = input.readLine()
                while (line != null) {
                    res.append(line)
                    line = input.readLine()
                }
                input.close()
            } finally {
                connection.disconnect()
            }
            return res.toString()
        }
    }
}
