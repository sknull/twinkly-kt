package de.visualdigits.kotlin.util

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object TimeUtil {

    /**
     * Converts the given seconds since midnight in UTC to the local date time in the current time zone of the machine.
     */
    fun offsetDateTimeOfSecondsFromMidnight(secondsOfDayInUtc: Int): OffsetDateTime {
        val h = secondsOfDayInUtc / 3600
        val remainingSeconds = secondsOfDayInUtc % 3600
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        return OffsetDateTime
            .now()
            .withNano(0)
            .withOffsetSameInstant(ZoneOffset.UTC) // !! order matters as the time zone is convert right in this method !!
            .withHour(h)
            .withMinute(m)
            .withSecond(s)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toOffsetDateTime()
    }

    /**
     * Returns the second of the day for the given hour and minute or of now when hour and minute are omitted.
     * Hour and minute are assumed to be in the current time zone of the machine and will be converted to UTC time zone
     * as xled device to be in zulu time.
     */
    fun utcSecondsAfterMidnight(
        hour: Int? = null,
        minute: Int?= null,
        second: Int= 0
    ): Int {
        return if (hour != null && minute != null) {
            OffsetDateTime
                .now()
                .withHour(hour)
                .withMinute(minute)
                .withSecond(second)
                .withNano(0)
                .atZoneSameInstant(ZoneOffset.UTC)
                .toLocalTime()
                .toSecondOfDay()
        } else {
            OffsetDateTime
                .now()
                .withSecond(0)
                .withNano(0)
                .atZoneSameInstant(ZoneOffset.UTC)
                .toLocalTime()
                .toSecondOfDay()
        }
    }
}
