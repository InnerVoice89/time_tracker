package com.tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Dto запроса пользователя на конкретный период
 */
@Getter
@Setter
public class TasksInPeriodRq {
    /**
     * Идентификатор пользователя
     */
    private long userId;
    /**
     * Начало периода
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodStart;
    /**
     * Окончание периода
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

}
