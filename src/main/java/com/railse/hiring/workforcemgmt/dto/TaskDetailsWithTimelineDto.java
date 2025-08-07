package com.railse.hiring.workforcemgmt.dto;

import java.util.List;

import lombok.Data;

@Data
public class TaskDetailsWithTimelineDto {
	private TaskManagementDto task;
	private List<TaskEvent> timeline;
}
