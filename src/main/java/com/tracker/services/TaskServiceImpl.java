package com.tracker.services;

import com.tracker.UserContext;
import com.tracker.dao.TaskDao;
import com.tracker.dao.UserDao;
import com.tracker.dto.*;
import com.tracker.exceptions.PersistenceException;
import com.tracker.utils.TrackerUtils;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;
    private final DataSource dataSource;

    @Override
    public long startTask(String taskName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId = UserContext.getUser().getId();
               if(!taskDao.checkActiveTasks(userId,connection)){
                   throw new PersistenceException("Необходимо закрыть активные задачи");
               }
                    RequestTaskDto taskDto = RequestTaskDto.builder()
                            .userId(UserContext.getUser().getId())
                            .taskName(taskName)
                            .build();

                long id = taskDao.createTask(taskDto, connection);
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
                throw e;
            }
        }
    }

    @Override
    public ShowTaskInfoRs showTasksById(long taskId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            List<TaskInfo> tasks = taskDao.findInfoByTask(taskId, UserContext.getUser().getTimeZone(), connection);
            return ShowTaskInfoRs.builder()
                    .intervalTasks(tasks)
                    .totalDuration(TrackerUtils.computeDuration(tasks))
                    .build();
        }
    }

    @Override
    public ShowTaskInfoRs getTasksByUserId(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            List<TaskInfo> taskList = taskDao.findTasksByUserId(id, UserContext.getUser().getTimeZone(), connection);
            return ShowTaskInfoRs.builder()
                    .intervalTasks(taskList)
                    .totalDuration(TrackerUtils.computeDuration(taskList))
                    .build();
        }
    }

    @Override
    public void cleanAllTasksByUserId(long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            taskDao.cleanAllTasksByUserId(id, connection);
        }
    }

    @Override
    public void deleteTaskByTaskId(long taskId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            taskDao.deleteTaskByTaskId(taskId, connection);
        }
    }

    @Override
    public ShowTaskInfoRs findTasksInPeriod(TasksInPeriodRq request) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (request.getPeriodStart().isAfter(request.getPeriodEnd())) {
                throw new IllegalArgumentException("Некорректный период");
            }
            String adminTimeZone = UserContext.getUser().getTimeZone();
            if (adminTimeZone == null)
                throw new IllegalStateException("Отсутствует временная зона у пользователя");

            Instant startTimeUtcRq = request.getPeriodStart().atZone(ZoneId.of(adminTimeZone)).toInstant();
            Instant endTimeUtcRq = request.getPeriodEnd().atZone(ZoneId.of(adminTimeZone)).toInstant();
            List<TaskInfo> tasks = taskDao.findTasksByUserId(request.getUserId(),
                    UserContext.getUser().getTimeZone(), connection);
            List<TaskInfo> resultList = new ArrayList<>();
            Instant now = Instant.now();
            for (TaskInfo entity : tasks) {

                Instant taskStart = entity.getStartTime().toInstant();
                Instant taskEnd = entity.getEndTime() != null ? entity.getEndTime().toInstant() : now;
                Instant startBound = taskStart.isAfter(startTimeUtcRq)
                        ? taskStart
                        : startTimeUtcRq;
                Instant endBound = taskEnd.isBefore(endTimeUtcRq)
                        ? taskEnd
                        : endTimeUtcRq;
                if (startBound.isBefore(endBound)) {
                    Duration duration = Duration.between(startBound, endBound);
                    TaskInfo taskInfo = TaskInfo.builder()
                            .taskName(entity.getTaskName())
                            .startTime(TrackerUtils.convertInstantToOffsetDT(startBound, adminTimeZone))
                            .endTime(TrackerUtils.convertInstantToOffsetDT(endBound, adminTimeZone))
                            .duration(TrackerUtils.correctDuration(duration))
                            .build();
                    resultList.add(taskInfo);
                }
            }
            return ShowTaskInfoRs.builder()
                    .intervalTasks(resultList)
                    .totalDuration(TrackerUtils.computeDuration(resultList))
                    .build();
        }
    }

    @Override
    public void putPauseResume(long taskId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long userId = UserContext.getUser().getId();
                taskDao.putPauseResume(taskId, userId, connection);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }
}