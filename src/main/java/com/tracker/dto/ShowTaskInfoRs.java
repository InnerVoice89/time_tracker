package com.tracker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
/**
 * Dto ответа пользователю информации по задачам
 */
@Getter
@Setter
@Builder
public class ShowTaskInfoRs {
    /**
     * Список задач
     */
    private List<TaskInfo> intervalTasks;
    /**
     * Продолжительность всех задач
     */
    private String totalDuration;
}
