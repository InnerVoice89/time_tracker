package com.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseInfoByTask {

    private String taskName;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String duration;
    private String errorMessage;


}
