package com.tracker.services;

import com.tracker.dao.TaskDao;
import com.tracker.dao.UserDao;
import com.tracker.dto.RequestDto;
import com.tracker.dto.ResponseInfoByTask;
import com.tracker.models.TaskEntity;
import com.tracker.models.User;
import com.tracker.utils.TrackerUtils;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;
    private final UserDao userDao;
    private final DataSource dataSource;

    @Override
    public long startTask(RequestDto request) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long id = taskDao.createTask(request, connection);
                connection.commit();
                return id;
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    @Override
    public void endTask(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            taskDao.endTask(id, connection);
            connection.commit();
        }
    }

    @Override
    public ResponseInfoByTask showInfoTask(RequestDto request) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            TaskEntity task = taskDao.findTaskById(request.getId(), connection);
            if (task == null) throw new RuntimeException("Такого задания не найдено");
            User user = userDao.findUserByUsername(request.getUsername(),connection);
            return TrackerUtils.toResponseDto(task, user);
        }
    }
}
