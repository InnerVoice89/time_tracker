package com.tracker.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ShowTaskInfoRs {

    private List<TaskInfo> intervalTasks;
    private String totalDuration;
}
