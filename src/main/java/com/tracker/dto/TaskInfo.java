package com.tracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskInfo {

    private long id;
    private String taskName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String duration;
    private long userId;
    private String errorMessage;

}
