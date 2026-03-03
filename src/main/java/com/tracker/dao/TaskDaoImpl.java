package com.tracker.dao;


import com.tracker.UserContext;
import com.tracker.dto.RequestTaskDto;
import com.tracker.dto.TaskEntity;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TaskDaoImpl implements TaskDao {


    @Override
    public long createTask(RequestTaskDto request, Connection connection) throws SQLException {
        String sql = "insert into task_table " +
                "(task_name,username,start_task) values(?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.getTaskName());
            ps.setString(2, UserContext.getUser().getUsername());
            ps.setTimestamp(3, Timestamp.from(Instant.now()));
            int affectedRow = ps.executeUpdate();
            if (affectedRow == 0)
                throw new SQLException("Создание задачи не удалось, строка не добавлена");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new RuntimeException("Ошибка при создании задачи");
                }
            }
        }
    }

    @Override
    public void endTask(long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("update task_table " +
                "set end_task=? where id=?")) {
            ps.setTimestamp(1, Timestamp.from(Instant.now()));
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    @Override
    public TaskEntity findTaskById(long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("select from task_table " +
                "where id=?")) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return new TaskEntity(
                    rs.getLong("id"),
                    rs.getString("task_name"),
                    rs.getTimestamp("start_task").toInstant(),
                    rs.getTimestamp("end_task").toInstant(),
                    rs.getLong("user_id")
            );
        }
    }

    @Override
    public void cleanAllTasksById(long id, Connection connection) throws SQLException {
        String sql = "delete from task_table where user_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();

        }
    }

    @Override
    public List<TaskEntity> findTasksByUserId(long id, Connection connection)
            throws SQLException {
        String sql = "select task_name,user_id,start_task,end_task from task_table where user_id=? ";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, id);
        ResultSet rs = ps.executeQuery();
        List<TaskEntity> entities = new ArrayList<>();
        while (rs.next()) {
            TaskEntity entity = new TaskEntity(
                    rs.getLong("id"),
                    rs.getString("task_name"),
                    rs.getTimestamp("start_task").toInstant(),
                    rs.getTimestamp("end_task").toInstant(),
                    rs.getLong("user_id")
            );
            entities.add(entity);
        }
        return entities;
    }
}
