package com.tracker.dao;


import com.tracker.dto.RequestTaskDto;
import com.tracker.dto.TaskInfo;
import com.tracker.exceptions.PersistenceException;
import com.tracker.utils.TrackerUtils;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class TaskDaoImpl implements TaskDao {

    @Override
    public long createTask(RequestTaskDto request, Connection connection) throws SQLException {
        String insertTaskTable = "insert into task_table " +
                "(task_name,user_id,start_task) values(?,?,?)";

        String insertTimeIntervalTable = "insert into time_interval_table (" +
                "task_id,start_time) values (?,?)";

        long taskId;
        Instant now = Instant.now();
        try (PreparedStatement tt = connection.prepareStatement(insertTaskTable, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement it = connection.prepareStatement(insertTimeIntervalTable)) {
            tt.setString(1, request.getTaskName());
            tt.setLong(2, request.getUserId());
            tt.setTimestamp(3, Timestamp.from(now));
            int affectedRow = tt.executeUpdate();
            if (affectedRow == 0)
                throw new SQLException("Создание задачи не удалось, строка не добавлена");
            try (ResultSet rs = tt.getGeneratedKeys()) {
                if (rs.next()) {
                    taskId = rs.getLong(1);
                } else {
                    throw new PersistenceException("Ошибка при создании задачи");
                }
            }
            it.setLong(1, taskId);
            it.setTimestamp(2, Timestamp.from(now));
            it.executeUpdate();

            return taskId;
        }

    }

    @Override
    public void endTask(long id, Connection connection) throws SQLException {
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
        }
    }

    @Override
    public List<TaskInfo> findInfoByTask(long taskId, String timeZone, Connection connection) throws SQLException {
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
        }

    }

    @Override
    public List<TaskInfo> findTasksByUserId(long id, String timeZone, Connection connection)
            throws SQLException {
        String sql = "select tit.start_time,tit.end_time,tit.task_id,tt.task_name,user_id " +
                "from time_interval_table tit " +
                "join task_table tt on tt.id = tit.task_id " +
                "where tt.user_id=? order by start_time";

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
                    } else {
                        duration = Duration.between(taskStartInst, now);
                    }
                    taskInfo.setDuration(TrackerUtils.correctDuration(duration));
                    entities.add(taskInfo);
                }
                return entities;
            }
        }
    }

    @Override
    public void putPauseResume(long taskId, long userId, Connection connection) throws SQLException {
        Instant now = Instant.now();
        String checkTask = " select 1 from task_table where id = ? and user_id = ? and end_task is null";

        try (PreparedStatement checkPs = connection.prepareStatement(checkTask)) {
            checkPs.setLong(1, taskId);
            checkPs.setLong(2, userId);
            if (!checkPs.executeQuery().next()) {
                throw new PersistenceException("Задача не найдена или завершена");
            }
        }
        String pauseSql = "update time_interval_table set end_time = ? where task_id = ? and end_time is null";

        try (PreparedStatement pausePs = connection.prepareStatement(pauseSql)) {
            pausePs.setTimestamp(1, Timestamp.from(now));
            pausePs.setLong(2, taskId);
            int updated = pausePs.executeUpdate();
            if (updated == 0) {
                if (!checkActiveTasks(userId, connection))
                    throw new PersistenceException("Необходимо закрыть активные задачи");
                String resumeSql = "insert into time_interval_table (task_id, start_time) values (?, ?)";

                try (PreparedStatement resumePs = connection.prepareStatement(resumeSql)) {
                    resumePs.setLong(1, taskId);
                    resumePs.setTimestamp(2, Timestamp.from(now));
                    resumePs.executeUpdate();
                }
            }
        }
    }

    @Override
    public void cleanAllTasksByUserId(long id, Connection connection) throws SQLException {
        String sql = "delete from task_table where user_id=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteTaskByTaskId(long taskId, Connection connection) throws SQLException {
        String sql = "delete from task_table where id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            int deleted = ps.executeUpdate();
            if (deleted == 0)
                throw new PersistenceException("Задачи не найдено");
        }
    }

    @Override
    public boolean checkActiveTasks(long userId, Connection connection) throws SQLException {
        String sql = "select 1 from time_interval_table tit " +
                "join task_table tt on tit.task_id=tt.id " +
                "where tt.user_id=? and tit.end_time is null ";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        }

    }
}
