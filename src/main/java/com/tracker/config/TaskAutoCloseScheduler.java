package com.tracker.config;

import com.tracker.exceptions.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер планировщика для автоматического обслуживания задач.
 * <p>
 * Периодически выполняет проверку незавершённых задач и автоматически
 * закрывает те из них, которые не были завершены пользователем до конца дня
 * (по временной зоне пользователя).
 */
@RequiredArgsConstructor
public class TaskAutoCloseScheduler {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TransactionManager transactionManager;
    private static final Logger log = LoggerFactory.getLogger(TaskAutoCloseScheduler.class);

    /**
     * Запускает планировщик проверки незавершённых задач.
     */
    public void startScheduler() {
        long period = Duration.ofMinutes(10).toMillis();
        scheduler.scheduleAtFixedRate(() ->
                {
                    try {
                        log.info("Проверка на наличие незакрытых задач");
                        finishUnclosedTasks();
                        log.info("Закрытие незавершенных задач прошло успешно");
                    } catch (Exception e) {
                        log.warn("Проблема с авто-закрытием задач ", e);
                    }
                }, 0, period, TimeUnit.MILLISECONDS
        );
    }

    /**
     * Завершает задачи, которые остались незакрытыми после окончания дня.
     * Метод выполняет следующие действия:
     * -Находит задачи, у которых отсутствует время завершения.
     * -Определяет конец дня для пользователя с учётом его временной зоны.
     * -Если текущее время позже конца дня — задача автоматически закрывается.
     * -Обновляет таблицы задач и интервалов времени.
     */
    public void finishUnclosedTasks() throws SQLException {
        transactionManager.executeInTransaction(connection -> {
            try (PreparedStatement checkPs = connection.prepareStatement(
                    "select tt.id as taskId,tt.start_task,ut.timezone " +
                            "from task_table tt " +
                            "join user_table ut on tt.user_id=ut.id " +
                            "where tt.end_task is null");
                 PreparedStatement updateTaskTablePs = connection.prepareStatement(
                         "update task_table set end_task = ? where id = ?");
                 PreparedStatement updateIntervalTablePs = connection.prepareStatement(
                         "update time_interval_table set end_time=? where task_id=? and end_time is null")
            ) {
                ResultSet rs = checkPs.executeQuery();
                Instant now = Instant.now();
                while (rs.next()) {
                    long taskId = rs.getLong("taskId");
                    Instant startTask = rs.getTimestamp("start_task").toInstant();
                    ZoneId userTimeZone = ZoneId.of(rs.getString("timezone"));
                    if (startTask == null)
                        throw new PersistenceException("Задача не была запущена");
                    ZonedDateTime endOfDay = startTask
                            .atZone(userTimeZone)
                            .toLocalDate()
                            .plusDays(1)
                            .atStartOfDay(userTimeZone);
                    Instant endOfDayUtc = endOfDay.toInstant();
                    if (now.isAfter(endOfDayUtc)) {
                        updateTaskTablePs.setTimestamp(1, Timestamp.from(endOfDayUtc));
                        updateTaskTablePs.setLong(2, taskId);
                        updateTaskTablePs.addBatch();

                        updateIntervalTablePs.setTimestamp(1, Timestamp.from(endOfDayUtc));
                        updateIntervalTablePs.setLong(2, taskId);
                        updateIntervalTablePs.addBatch();
                    }
                }

                updateTaskTablePs.executeBatch();
                updateIntervalTablePs.executeBatch();

            }
            return null;
        });
    }

    /**
     * Остановка планировщика
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}
