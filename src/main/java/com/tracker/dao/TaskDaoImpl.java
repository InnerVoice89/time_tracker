package com.tracker.dao;


import com.tracker.dto.RequestDto;
import com.tracker.models.TaskEntity;

import java.sql.*;
import java.time.Instant;

public class TaskDaoImpl implements TaskDao {


    @Override
    public long createTask(RequestDto request, Connection connection) throws SQLException {
        String sql = "insert into task_table " +
                "(task_name,username,start_task) values(?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, request.getTaskName());
        ps.setString(2, request.getUsername());
        ps.setTimestamp(3, Timestamp.from(Instant.now()));
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        long id = 0;
        while (rs.next()) {
            id = rs.getLong(1);
        }
        return id;
    }

    @Override
    public TaskEntity findTaskById(long id, Connection connection) {
        return null;
    }

    @Override
    public void endTask(long id, Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("update task_table " +
                "set end_task=? where id=?");
        ps.setTimestamp(1, Timestamp.from(Instant.now()));
        ps.setLong(2, id);
        ps.executeUpdate();

    }
}
