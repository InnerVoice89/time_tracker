package com.tracker.services;

import com.tracker.UserContext;
import com.tracker.config.TransactionManager;
import com.tracker.dao.TaskDao;
import com.tracker.dto.*;
import com.tracker.exceptions.IllegalRequestException;
import com.tracker.utils.TrackerUtils;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskDao taskDao;
    private final TransactionManager transactionManager;

    @Override
    public long startTask(String taskName) {
        return transactionManager.executeInTransaction(connection -> {

            // ID текущего пользователя берем из сессии
            long userId = UserContext.getUser().getId();
            if (taskDao.checkActiveTasks(userId, connection)) {
                throw new IllegalStateException("Необходимо закрыть активные задачи");
            }
            RequestTaskDto taskDto = RequestTaskDto.builder()
                    .userId(userId)
                    .taskName(taskName)
                    .build();

            return taskDao.createTask(taskDto, connection);
        });
    }

    @Override
    public void endTask(long id) {
        transactionManager.executeInTransaction(connection -> {

            taskDao.endTask(id, connection);
            return null;
        });
    }

    @Override
    public ShowTaskInfoRs showTasksById(long taskId) {
        return transactionManager.executeRead(connection -> {

            List<TaskInfo> tasks = taskDao.findInfoByTask(taskId, UserContext.getUser().getTimeZone(), connection);
            return ShowTaskInfoRs.builder()
                    .intervalTasks(tasks)
                    .totalDuration(TrackerUtils.computeDuration(tasks))
                    .build();
        });
    }

    @Override
    public ShowTaskInfoRs getTasksByUserId(long id) {
        return transactionManager.executeRead(connection -> {

            List<TaskInfo> taskList = taskDao.findTasksByUserId(id, UserContext.getUser().getTimeZone(), connection);
            return ShowTaskInfoRs.builder()
                    .intervalTasks(taskList)
                    .totalDuration(TrackerUtils.computeDuration(taskList))
                    .build();
        });
    }


    @Override
    public void cleanAllTasksByUserId(long id) {
        transactionManager.executeInTransaction(connection -> {

            taskDao.cleanAllTasksByUserId(id, connection);
            return null;
        });
    }


    @Override
    public void deleteTaskByTaskId(long taskId) {
        transactionManager.executeInTransaction(connection -> {

            taskDao.deleteTaskByTaskId(taskId, connection);
            return null;
        });
    }


    @Override
    public ShowTaskInfoRs findTasksInPeriod(TasksInPeriodRq request) {
        return transactionManager.executeRead(connection -> {

            // Проверка корректности запроса
            validateRequest(request);
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
            // Проверка,входят ли интервалы задач в рамки запроса.Если да,то создаем объект с интервалом пересечения дат
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
        });
    }


    @Override
    public void putPauseResume(long taskId) {
        transactionManager.executeInTransaction(connection -> {
            long userId = UserContext.getUser().getId();
            taskDao.putPauseResume(taskId, userId, connection);
            return null;
        });
    }

    /**
     * Проверка корректности запроса на получение информации по периоду
     */
    public void validateRequest(TasksInPeriodRq request) {
        if (request.getUserId() == 0)
            throw new IllegalRequestException("ID пользователя не может быть пустым");
        if (request.getPeriodStart() == null)
            throw new IllegalRequestException("Необходимо задать дату начала периода");
        if (request.getPeriodEnd() == null)
            throw new IllegalRequestException("Необходимо задать дату окончания периода");
    }

}