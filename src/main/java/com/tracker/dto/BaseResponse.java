package com.tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * Универсальный Dto для ответа пользователю
 */
@Getter
@Setter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse {
    /**
     * Состояние ответа
     */
    private boolean success;
    /**
     * Данные,если таковые имеются
     */
    private Object data;
    /**
     * Информация в случае ошибки
     */
    private String error;
    /**
     * Общее сообщение
     */
    private String message;

}
