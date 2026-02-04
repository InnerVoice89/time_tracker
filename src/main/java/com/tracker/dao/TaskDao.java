package com.tracker.dao;

import com.tracker.dto.RequestDto;
import com.tracker.models.TaskEntity;

import java.sql.Connection;
import java.sql.SQLException;

public interface TaskDao {

    long createTask(RequestDto request, Connection connection) throws SQLException;

    TaskEntity findTaskById(long id, Connection connection);

    void endTask(long id, Connection connection) throws SQLException;

}
