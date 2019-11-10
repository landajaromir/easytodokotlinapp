package com.kotlin.education.android.easytodo.utils

import java.text.SimpleDateFormat
import java.util.*


/**
 * The utility for managing dates and times.
 */
class DateUtils {

    companion object {
        /**
         * The format of a time stored for czech language
         */
        private val DATE_FORMAT_CS = "dd. MM. yyyy"

        // TODO Homework: This method only shows the czech date format.
        // Change it to show also english based on device language.

        /**
         * Converts a Unix timestamp to String format.
         */
        fun getDateString(unixTime: Long): String{
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = unixTime * 1000
            val format = SimpleDateFormat(DATE_FORMAT_CS)
            return format.format(calendar.getTime())
        }

        /**
         * Converts the date represented by year, month and day to Unix Timestamp
         */
        fun getUnixTime(year: Int, month: Int, day: Int): Long {
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            return calendar.timeInMillis / 1000
        }
    }

}