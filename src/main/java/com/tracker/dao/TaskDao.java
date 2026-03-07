package com.tracker.dao;

import com.tracker.dto.RequestTaskDto;
import com.tracker.dto.TaskEntity;
import com.tracker.dto.TaskInfo;
import com.tracker.dto.TasksInPeriodRq;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TaskDao {

    long createTask(RequestTaskDto request, Connection connection) throws SQLException;

    void endTask(long id, Connection connection) throws SQLException;

    List<TaskInfo> findInfoByTask(long taskId, String timeZone, Connection connection) throws SQLException;

    void cleanAllTasksByUserId(long id, Connection connection) throws SQLException;

    List<TaskInfo> findTasksByUserId(long id, String timeZone, Connection connection) throws SQLException;

    void putPauseResume(long taskId, long userId, Connection connection) throws SQLException;

    void deleteTaskByTaskId(long taskId, Connection connection) throws SQLException;

    boolean checkActiveTasks(long userId, Connection connection) throws SQLException;
}
