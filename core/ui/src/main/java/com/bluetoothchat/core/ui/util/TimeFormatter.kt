package com.bluetoothchat.core.ui.util

import android.content.Context
import com.bluetoothchat.core.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeFormatter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val locale = Locale.getDefault()
    private val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale)
    private val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)

    fun formatTimeDate(timestamp: Long, type: TimeFormatType): String = when (type) {
        TimeFormatType.ONLY_TIME -> formatTime(timestamp)
        TimeFormatType.TIME_IF_TODAY_DATE_OTHERWISE -> {
            val isToday = areTheSameDay(timestamp, System.currentTimeMillis())
            if (isToday) {
                formatTime(timestamp)
            } else {
                formatDate(timestamp)
            }
        }

        TimeFormatType.TODAY_OR_DATE -> {
            val isToday = areTheSameDay(timestamp, System.currentTimeMillis())
            if (isToday) {
                context.getString(R.string.chat_today)
            } else {
                formatDate(timestamp)
            }
        }
    }

    fun formatTime(timestamp: Long) = timeFormat.format(Date(timestamp))

    fun formatDate(timestamp: Long) = dateFormat.format(Date(timestamp))

    fun areTheSameDay(timestamp1: Long, timestamp2: Long) =
        getStartTimestampOfDay(timestamp1) == getStartTimestampOfDay(timestamp2)

    private fun getStartTimestampOfDay(timestamp: Long): Long {
        return with(Calendar.getInstance()) {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }
    }

}

enum class TimeFormatType {
    ONLY_TIME, TIME_IF_TODAY_DATE_OTHERWISE, TODAY_OR_DATE
}
