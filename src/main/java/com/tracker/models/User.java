package com.tracker.models;

import com.tracker.dto.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class User {

    private long id;
    private String username;
    private String password;
    private String timeZone;
    private Set<Role> roles;


}
