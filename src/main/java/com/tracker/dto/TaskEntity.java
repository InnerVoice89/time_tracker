package com.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity {

    private long id;
    private String taskName;
    private Instant taskStart;
    private Instant taskEnd;
    private long userId;

}
