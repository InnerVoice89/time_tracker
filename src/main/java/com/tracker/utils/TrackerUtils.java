package com.tracker.utils;


import com.tracker.dto.TaskInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

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
            sb.append(seconds).append(" sec.");
        return sb.toString();

    }

    public static OffsetDateTime convertInstantToOffsetDT(Instant instant, String timeZone) {
        if (timeZone == null)
            throw new IllegalArgumentException("ZoneId не должен отсутствовать");
        return instant.atZone(ZoneId.of(timeZone)).toOffsetDateTime();
    }

    public static TaskInfo mapToTaskInfo(ResultSet rs, String timeZone) throws SQLException {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTaskId(rs.getLong("task_id"));
        taskInfo.setTaskName(rs.getString("task_name"));
        taskInfo.setUserId(rs.getLong("user_id"));

        Instant taskStart = rs.getTimestamp("start_time").toInstant();

        taskInfo.setStartTime(convertInstantToOffsetDT(taskStart, timeZone));
        Timestamp taskEnd = rs.getTimestamp("end_time");
        Duration duration;
        if (taskEnd != null) {
            duration = Duration.between(taskStart, taskEnd.toInstant());
            taskInfo.setEndTime(convertInstantToOffsetDT(taskEnd.toInstant(), timeZone));
        } else {
            duration = Duration.between(taskStart, Instant.now());
        }
        taskInfo.setDuration(correctDuration(duration));
        return taskInfo;
    }

    public static String computeDuration(List<TaskInfo> list) {
        Instant now = Instant.now();
        Duration duration = list.stream()
                .map(task -> {
                    Instant start = task.getStartTime().toInstant();
                    Instant end = task.getEndTime() == null ? now
                            : task.getEndTime().toInstant();
                    return Duration.between(start, end);
                })
                .reduce(Duration.ZERO, Duration::plus);
        return correctDuration(duration);
    }
}
