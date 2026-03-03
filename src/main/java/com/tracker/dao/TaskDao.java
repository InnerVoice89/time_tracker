package com.tracker.dao;

import com.tracker.dto.RequestTaskDto;
import com.tracker.dto.TaskEntity;
import com.tracker.dto.TasksInPeriodRq;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TaskDao {

    long createTask(RequestTaskDto request, Connection connection) throws SQLException;

    TaskEntity findTaskById(long id, Connection connection) throws SQLException;

    void endTask(long id, Connection connection) throws SQLException;

    void cleanAllTasksById(long id, Connection connection) throws SQLException;

    List<TaskEntity> findTasksByUserId(long id, Connection connection) throws SQLException;
}
