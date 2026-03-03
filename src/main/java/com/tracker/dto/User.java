package com.tracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class User {

    private Long id;
    private String username;
    private String password;
    private String timeZone;
    private Set<Role> roles;
}
