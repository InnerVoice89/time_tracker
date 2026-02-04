package com.tracker.utils;


import com.tracker.dto.ResponseInfoByTask;
import com.tracker.models.TaskEntity;
import com.tracker.models.User;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class TrackerUtils {

    public static String correctDuration(Duration duration) {

        if (duration.isNegative() || duration.isZero())
            throw new RuntimeException("Некорректная продолжительность работы");

        StringBuilder sb = new StringBuilder();
        long hours = duration.getSeconds() / 3600;
        if (hours >= 1)
            sb.append(hours).append(" ч. ");
        long minutes = duration.getSeconds() % 3600 / 60;
        if (minutes >= 1)
            sb.append(minutes).append(" m. ");
        long seconds = duration.getSeconds() % 3600 % 60;
        if (seconds > 0)
            sb.append(seconds).append("sec");

        return sb.toString();

    }
    public static OffsetDateTime convertInstantToOffsetDT(Instant instant, String zoneId) {
        return instant.atZone(ZoneId.of(zoneId)).toOffsetDateTime();

    }

    public static ResponseInfoByTask toResponseDto(TaskEntity entity, User user) {
        Duration duration = Duration.between(entity.getTaskStart(), entity.getTaskEnd());
        return new ResponseInfoByTask(entity.getTaskName()
                , convertInstantToOffsetDT(entity.getTaskStart(), user.getTimeZone())
                , convertInstantToOffsetDT(entity.getTaskEnd(), user.getTimeZone())
                , correctDuration(duration), null
        );

    }
}
