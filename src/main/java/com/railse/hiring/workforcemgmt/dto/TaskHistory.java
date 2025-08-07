package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

@Data
public class TaskHistory {
	private Long id;
    private Long taskId;
    private String action; 
    private Long timestamp;
}
