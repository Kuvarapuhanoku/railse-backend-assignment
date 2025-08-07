package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskEvent {
	private Long timestamp;
    private String type;
    private String message;
}
