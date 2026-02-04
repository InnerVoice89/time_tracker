package com.tracker.dto;

import java.util.Collection;

public enum Role {
    USER, ADMIN;

    public static boolean has(Role required, Collection<Role> roles) {
        return roles != null && roles.contains(required);
    }

}