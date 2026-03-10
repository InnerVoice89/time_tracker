package com.tracker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;

/**
 * Dto c информацией о задаче
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskInfo {
    /**
     * Идентификатор задачи
     */
    private Long taskId;
    /**
     * Название задачи
     */
    private String taskName;
    /**
     * Время начала задачи со смещением
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime startTime;
    /**
     * Время окончания задачи со смещением
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private OffsetDateTime endTime;
    /**
     * Продолжительность данной задачи
     */
    private String duration;
    /**
     * Идентификатор пользователя
     */
    private Long userId;
    /**
     * Сообщение об ошибке
     */
    private String errorMessage;

}
