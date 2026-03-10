package com.tracker.dto;

import java.util.Collection;

/**
 * Роли пользователя
 */
public enum Role {
    USER, ADMIN;
    /**
     * Сопоставление ролей
     */
    public static boolean has(Role required, Collection<Role> roles) {
        return roles != null && roles.contains(required);
    }

}