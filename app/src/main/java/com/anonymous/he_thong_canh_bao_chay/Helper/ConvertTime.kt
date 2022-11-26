package com.anonymous.he_thong_canh_bao_chay.Helper

import java.util.*

object ConvertTime {
    fun convertDateTimeToUnix(
        year: Int,
        month: Int,
        date: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Long {

        val timeZone = TimeZone.getTimeZone("UTC")
        val calendar: Calendar = Calendar.getInstance(timeZone)
        calendar.set(year, month, date, hour, minute, second)
        return calendar.getTimeInMillis()
    }
}