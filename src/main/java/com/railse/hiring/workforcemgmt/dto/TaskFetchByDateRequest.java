package com.railse.hiring.workforcemgmt.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TaskFetchByDateRequest {
	private Long startDate;
	private Long endDate;
	private List<Long> assigneeIds;
}
