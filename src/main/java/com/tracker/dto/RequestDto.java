package com.tracker.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDto {

    private long id;
    private String taskName;
    private String username;

}
