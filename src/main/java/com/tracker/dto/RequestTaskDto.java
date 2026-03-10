package com.tracker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Dto запроса создания новой задачи
 */
@Getter
@Setter
@Builder
public class RequestTaskDto {
    /**
     * Id пользователя
     */
    private long userId;
    /**
     * Название задачи
     */
    private String taskName;

}
