package org.tbch.tbchutils.util;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getFormattedTime(long timestamp, boolean displayDiff, boolean showSeconds) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime utcDateTime = instant.atZone(ZoneOffset.UTC);
        if (displayDiff) {
            Duration duration = Duration.between(instant, Instant.now());
            long days = duration.toDays();
            long hours = duration.toHours() % 24;
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;

            String timeDiff = "";
            if (days > 0) {timeDiff += days + (days == 1 ? " day " : " days ");}
            if (hours > 0) {timeDiff += hours + (hours == 1 ? " hour " : " hours ");}
            if (minutes > 0 || !showSeconds) {timeDiff += minutes + (minutes == 1 ? " minute" : " minutes");}
            if (timeDiff.isEmpty() && showSeconds) {timeDiff = seconds + (seconds == 1 ? " second" : " seconds");}
            return timeDiff;
        } else {return formatter.format(utcDateTime);}
    }

    public static String convertSeconds(int seconds) {
        int days = seconds / 86400;
        int hours = (seconds / 3600) % 24;
        int minutes = (seconds / 60) % 60;

        String timePlayed = "";
        if (days > 0) {timePlayed += days + (days == 1 ? " day " : " days ");}
        if (hours > 0) {timePlayed += hours + (hours == 1 ? " hour " : " hours ");}
        if (minutes > 0 || (days == 0 && hours == 0 && minutes == 0)) {timePlayed += minutes + (minutes == 1 ? " minute" : " minutes");}
        return timePlayed;
    }
}
