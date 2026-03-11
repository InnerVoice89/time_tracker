package com.tracker.dao;

import com.tracker.dto.RequestTaskDto;
import com.tracker.dto.TaskInfo;
import com.tracker.exceptions.PersistenceException;
import com.tracker.utils.TrackerUtils;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO-класс доступа к данным по задачам
 */
public class TaskDao {

    /**
     * Создание новой задачи
     *
     * @param request Название задачи и ID текущего пользователя
     * @return id созданной задачи
     */
    public long createTask(RequestTaskDto request, Connection connection) {
        // Cоздание задачи
        String insertTaskTable = "insert into task_table " +
                "(task_name,user_id,start_task) values(?,?,?)";
        // Запуск интервала в таблице промежуточных работ над задачей
        String insertTimeIntervalTable = "insert into time_interval_table (" +
                "task_id,start_time) values (?,?)";

        long taskId;
        Instant now = Instant.now();
        try (PreparedStatement insertTT = connection.prepareStatement(insertTaskTable, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertTIT = connection.prepareStatement(insertTimeIntervalTable)) {
            insertTT.setString(1, request.getTaskName());
            insertTT.setLong(2, request.getUserId());
            insertTT.setTimestamp(3, Timestamp.from(now));
            int affectedRow = insertTT.executeUpdate();
            if (affectedRow == 0)
                throw new PersistenceException("Создание задачи не удалось, строка не добавлена");
            try (ResultSet rs = insertTT.getGeneratedKeys()) {
                if (rs.next()) {
                    taskId = rs.getLong(1);
                } else {
                    throw new PersistenceException("Ошибка при создании задачи");
                }
            }
            insertTIT.setLong(1, taskId);
            insertTIT.setTimestamp(2, Timestamp.from(now));
            insertTIT.executeUpdate();
            return taskId;
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка создания задачи", e);
        }
    }

    /**
     * Завершение задачи по идентификатору
     */
    public void endTask(long id, Connection connection) {
        try (PreparedStatement commonEndPs = connection.prepareStatement("update task_table " +
                "set end_task=? where id=?");
             PreparedStatement intervalEndPs = connection.prepareStatement("update time_interval_table " +
                     "set end_time=? where task_id=? and end_time is null")
        ) {
            Instant now = Instant.now();
            commonEndPs.setTimestamp(1, Timestamp.from(now));
            commonEndPs.setLong(2, id);
            int updated = commonEndPs.executeUpdate();
            if (updated == 0)
                throw new PersistenceException("Задача не найдена");

            intervalEndPs.setTimestamp(1, Timestamp.from(now));
            intervalEndPs.setLong(2, id);
            intervalEndPs.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка завершения задачи", e);
        }
    }

    /**
     * Поиск информации о задаче по ID
     *
     * @param timeZone временная зона администратора,берется из сессии
     * @return список временных интервалов по данной задаче
     */
    public List<TaskInfo> findInfoByTask(long taskId, String timeZone, Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(
                "select * from time_interval_table tit " +
                        "join task_table tt on tit.task_id=tt.id " +
                        "where tit.task_id=? order by start_time")) {
            ps.setLong(1, taskId);
            List<TaskInfo> tasks = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(TrackerUtils.mapToTaskInfo(rs, timeZone));
                }
            }
            return tasks;
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка получения информации по задаче", e);
        }
    }

    /**
     * Поиск информации по всем задачам пользователя
     *
     * @param timeZone временная зона администратора
     */
    public List<TaskInfo> findTasksByUserId(long id, String timeZone, Connection connection) {
        String sql = "select tit.start_time,tit.end_time,tit.task_id,tt.task_name,user_id " +
                "from time_interval_table tit " +
                "join task_table tt on tt.id = tit.task_id " +
                "where tt.user_id=? order by tit.start_time";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<TaskInfo> entities = new ArrayList<>();
                Instant now = Instant.now();
                while (rs.next()) {
                    TaskInfo taskInfo = new TaskInfo();
                    taskInfo.setTaskId(rs.getLong("task_id"));
                    taskInfo.setTaskName(rs.getString("task_name"));
                    Timestamp startTimeTs = rs.getTimestamp("start_time");
                    if (startTimeTs == null) {
                        throw new PersistenceException("Отсутствует начало интервала в задаче :" + id);
                    }
                    taskInfo.setStartTime(startTimeTs.toInstant().atZone(ZoneId.of(timeZone)).toOffsetDateTime());
                    taskInfo.setUserId(rs.getLong("user_id"));

                    Instant taskStartInst = startTimeTs.toInstant();
                    Timestamp taskEndTs = rs.getTimestamp("end_time");
                    Duration duration = null;
                    if (taskEndTs != null) {
                        Instant taskEndInst = taskEndTs.toInstant();
                        duration = Duration.between(taskStartInst, taskEndInst);
                        taskInfo.setEndTime(taskEndInst.atZone(ZoneId.of(timeZone)).toOffsetDateTime());
                        // Если интервал открыт,то продолжительность считается до текущего времени
                    } else {
                        duration = Duration.between(taskStartInst, now);
                    }
                    taskInfo.setDuration(TrackerUtils.correctDuration(duration));
                    entities.add(taskInfo);
                }
                return entities;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка получения информации по задачам пользователя", e);
        }
    }

    /**
     * Постановка на паузу в случае,если задача запущена.Если задача остановлена,то происходит запуск
     * нового интервала
     */

    public void putPauseResume(long taskId, long userId, Connection connection) {
        Instant now = Instant.now();
        String checkTask = " select 1 from task_table where id = ? and user_id = ? and end_task is null";
        try {
            try (PreparedStatement checkPs = connection.prepareStatement(checkTask)) {
                checkPs.setLong(1, taskId);
                checkPs.setLong(2, userId);
                if (!checkPs.executeQuery().next()) {
                    throw new PersistenceException("Задача не найдена или завершена");
                }
            }
            // Останавливаем незавершенную задачу
            String pauseSql = "update time_interval_table set end_time = ? where task_id = ? and end_time is null";

            try (PreparedStatement pausePs = connection.prepareStatement(pauseSql)) {
                pausePs.setTimestamp(1, Timestamp.from(now));
                pausePs.setLong(2, taskId);
                int updated = pausePs.executeUpdate();
                if (updated == 0) {
                    // Если задача остановлена,проверяем перед запуском наличие у пользователя открытых задач
                    if (checkActiveTasks(userId, connection))
                        throw new PersistenceException("Необходимо закрыть активные задачи");
                    String resumeSql = "insert into time_interval_table (task_id, start_time) values (?, ?)";

                    try (PreparedStatement resumePs = connection.prepareStatement(resumeSql)) {
                        resumePs.setLong(1, taskId);
                        resumePs.setTimestamp(2, Timestamp.from(now));
                        resumePs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("Ошибка паузы/запуска задачи", e);
        }
    }

    /**
     * Очистка всех задач пользователя
     */

    public void cleanAllTasksByUserId(long id, Connection connection) throws SQLException {
        String sql = "delete from task_table where user_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Удаление конкретной задачи
     */

    public void deleteTaskByTaskId(long taskId, Connection connection) throws SQLException {
        String sql = "delete from task_table where id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            int deleted = ps.executeUpdate();
            if (deleted == 0)
                throw new PersistenceException("Задачи не найдено");
        }
    }

    /**
     * Проверка пользователя на открытые задачи
     */

    public boolean checkActiveTasks(long userId, Connection connection) throws SQLException {
        String sql = "select 1 from time_interval_table tit " +
                "join task_table tt on tit.task_id=tt.id " +
                "where tt.user_id=? and tit.end_time is null ";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }

    }
}
