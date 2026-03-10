package com.tracker.services;

import com.tracker.dto.*;

import java.sql.SQLException;

/**
 * Сервис для управления задачами пользователей
 */
public interface TaskService {
    /**
     * Запуск новой задачи
     * @param taskName название задачи
     * @return id созданной задачи
     */
    long startTask(String taskName) throws SQLException;
    /**
     * Завершение задачи
     */
    void endTask(long id);
    /**
     * Получение информации по конкретной задаче
     */
    ShowTaskInfoRs showTasksById(long taskId);
    /**
     * Поиск информации по всем задачам пользователя
     *
     * @return список интервалов работ по всем задачам пользователя
     */
    ShowTaskInfoRs getTasksByUserId(long id);
    /**
     * Получение информации по работе пользователя в определенный период времени
     * @param request содержит информацию : id пользователя и временные рамки
     *
     * @return список заданий в данный период и общую продолжительность
     */
    ShowTaskInfoRs findTasksInPeriod(TasksInPeriodRq request);
    /**
     * Удаление всех задач пользователя
     */
    void cleanAllTasksByUserId(long id);
    /**
     * Постановка задачи на паузу/возобновление,исходя из её состояния
     */
    void putPauseResume(long taskId);
    /**
     * Удаление конкретной задачи
     */
    void deleteTaskByTaskId(long taskId);
}
