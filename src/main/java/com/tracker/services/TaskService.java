package com.tracker.services;

import com.tracker.dto.*;

import java.sql.SQLException;
import java.util.List;

public interface TaskService {

    long startTask(String taskName) throws SQLException;

    void endTask(long id) throws SQLException;

    ShowTaskInfoRs showTasksById(long taskId) throws SQLException;

    ShowTaskInfoRs getTasksByUserId(long id) throws SQLException;

    ShowTaskInfoRs findTasksInPeriod(TasksInPeriodRq request) throws SQLException;

    void cleanAllTasksByUserId(long id) throws SQLException;

    void putPauseResume(long taskId) throws SQLException;

    void deleteTaskByTaskId(long taskId) throws SQLException;
}
