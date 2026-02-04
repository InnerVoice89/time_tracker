package com.tracker.models;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TaskEntity {

    private long id;
    private String taskName;
    private Instant taskStart;
    private Instant taskEnd;
    private String username;



}
