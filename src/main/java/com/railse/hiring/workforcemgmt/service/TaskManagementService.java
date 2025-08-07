package com.railse.hiring.workforcemgmt.service;

import java.util.List;

import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;

public interface TaskManagementService {
	List<TaskManagementDto> createTasks(TaskCreateRequest request);
	   List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
	   String assignByReference(AssignByReferenceRequest request);
	   List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
	   TaskManagementDto findTaskById(Long id);
	   List<TaskManagementDto> getSmartDailyTasks(List<Long> assigneeIds, long startDate, long endDate);
	   void updateTaskPriority(UpdateTaskPriorityRequest request);
	   List<TaskManagementDto> getTasksByPriority(Priority priority);
}	
