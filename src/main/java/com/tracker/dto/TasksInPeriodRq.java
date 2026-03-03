package com.tracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TasksInPeriodRq {
    private long userId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;


}
