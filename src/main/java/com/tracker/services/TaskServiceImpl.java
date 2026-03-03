package com.tracker.services;

import com.tracker.UserContext;
import com.tracker.dao.TaskDao;
import com.tracker.dao.UserDao;
import com.tracker.dto.*;
import com.tracker.utils.TrackerUtils;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;
    private final DataSource dataSource;
    private final UserDao userDao;

    @Override
    public long startTask(RequestTaskDto request) throws SQLException {
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
            try {
                taskDao.endTask(id, connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
            }
        }
    }

    @Override
    public TaskInfo showTaskById(RequestTaskDto request) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            TaskEntity task = taskDao.findTaskById(request.getId(), connection);
            if (task == null) throw new RuntimeException("Такого задания не найдено");
            return TrackerUtils.toResponseDto(task, UserContext.getUser().getTimeZone());
        }
    }

    public void cleanAllTasksByUser(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                User user = userDao.findUserById(id, connection);
                taskDao.cleanAllTasksById(user.getId(), connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
            }
        }
    }

    public List<TaskInfo> findTasksInPeriod(TasksInPeriodRq request) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (request.getPeriodStart().isAfter(request.getPeriodEnd())) {
                throw new IllegalArgumentException("Некорректный период");
            }
            String adminTimeZone = UserContext.getUser().getTimeZone();
            Instant startDateUtcRq = request.getPeriodStart().atZone(ZoneId.of(adminTimeZone)).toInstant();
            Instant endDateUtcRq = request.getPeriodEnd().atZone(ZoneId.of(adminTimeZone)).toInstant();
            List<TaskEntity> tasks = taskDao.findTasksByUserId(request.getUserId(), connection);
            List<TaskInfo> resultList = new ArrayList<>();
            for (TaskEntity entity : tasks) {
                Instant taskEnd = entity.getTaskEnd() != null ? entity.getTaskEnd() : Instant.now();
                Instant startBound = entity.getTaskStart().isAfter(startDateUtcRq)
                        ? entity.getTaskStart()
                        : startDateUtcRq;
                Instant endBound = taskEnd.isBefore(endDateUtcRq)
                        ? taskEnd
                        : endDateUtcRq;
                if (startBound.isBefore(endBound)) {
                    Duration duration = Duration.between(startBound, endBound);
                    TaskInfo taskInfo = TaskInfo.builder()
                            .taskName(entity.getTaskName())
                            .duration(TrackerUtils.correctDuration(duration))
                            .build();
                    resultList.add(taskInfo);
                }
            }
            return resultList;
        }
    }
}
