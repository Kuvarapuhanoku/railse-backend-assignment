package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskComment {
	private Long id;
    private Long taskId;
    private Long userId;
    private String comment;
    private Long timestamp;
}
