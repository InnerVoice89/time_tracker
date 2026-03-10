package com.tracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Dto с информацией о пользователе
 */
@Getter
@Setter
public class User {
    /**
     * Id пользователя
     */
    private Long id;
    /**
     * username
     * Не может быть null
     */
    private String username;
    /**
     * Пароль
     * Не может быть null
     */
    private String password;
    /**
     * Временная зона
     * Не может быть null
     */
    private String timeZone;
    /**
     * Роли пользователя
     */
    private Set<Role> roles;
}
