package com.railse.hiring.workforcemgmt.dto;

import java.util.List;

import lombok.Data;

@Data
public class TaskFetchSmartRequest {
	private List<Long> assigneeIds;
    private long startDate;
    private long endDate;
}
