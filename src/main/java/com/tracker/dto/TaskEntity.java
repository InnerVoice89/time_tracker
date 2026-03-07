package com.tracker.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
public class TaskEntity {

    private long id;
    private String taskName;
    private Instant taskStart;
    private Instant taskEnd;
    private long userId;

}
