package com.tracker.scheduler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SchedulerManager {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(SchedulerManager.class);

    public void startScheduler() {
        long period = Duration.ofMinutes(10).toMillis();
        scheduler.scheduleAtFixedRate(() ->
                {
                    try {
                        log.info("Проверка на наличие незакрытых задач");
                        finishUnclosedTasks(dataSource);
                    } catch (Exception e) {
                        log.warn("Проблема с авто-закрытием задач ", e);
                    }
                }, 0, period, TimeUnit.MILLISECONDS
        );
    }

    public void finishUnclosedTasks(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement checkPs = connection.prepareStatement(
                    "select tt.id as taskId,tt.start_task,ut.timezone from task_table tt join" +
                            " user_table ut on tt.user_id=ut.id where tt.end_task is null");
                 PreparedStatement updatePs = connection.prepareStatement(
                         "update task_table set end_task = ? where id = ?")) {
                ResultSet rs = checkPs.executeQuery();
                while (rs.next()) {
                    long id = rs.getLong("id");
                    Instant startTask = rs.getTimestamp("start_task").toInstant();
                    ZoneId userTimeZone = ZoneId.of(rs.getString("timezone"));
                    if (startTask == null)
                        throw new RuntimeException("Задача не была запущена");
                    ZonedDateTime startWithZone = startTask.atZone(ZoneOffset.UTC)
                            .withZoneSameInstant(userTimeZone);
                    ZonedDateTime endOfDay = startWithZone.toLocalDate().atTime(23, 59)
                            .atZone(userTimeZone);
                    Instant endOfDayUtc = endOfDay.toInstant();
                    if (Instant.now().isAfter(endOfDayUtc)) {

                        updatePs.setTimestamp(1, Timestamp.from(endOfDayUtc));
                        updatePs.setLong(2, id);
                        updatePs.addBatch();

                    }

                }
                updatePs.executeBatch();
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
