package com.tracker.services;

import com.tracker.dto.RequestDto;
import com.tracker.dto.ResponseInfoByTask;

import java.sql.SQLException;

public interface TaskService {

    long startTask(RequestDto request) throws SQLException;

    void endTask(long id) throws SQLException;

    ResponseInfoByTask showInfoTask(RequestDto request) throws SQLException;

}
