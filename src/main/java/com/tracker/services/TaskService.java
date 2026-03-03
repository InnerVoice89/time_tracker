package com.tracker.services;

import com.tracker.dto.RequestTaskDto;
import com.tracker.dto.TaskInfo;

import java.sql.SQLException;

public interface TaskService {

    long startTask(RequestTaskDto request) throws SQLException;

    void endTask(long id) throws SQLException;

    TaskInfo showTaskById(RequestTaskDto request) throws SQLException;

}
