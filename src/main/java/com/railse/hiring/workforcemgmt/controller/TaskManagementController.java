package com.railse.hiring.workforcemgmt.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.railse.hiring.workforcemgmt.common.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.AssignByReferenceRequest;
import com.railse.hiring.workforcemgmt.dto.TaskComment;
import com.railse.hiring.workforcemgmt.dto.TaskCreateRequest;
import com.railse.hiring.workforcemgmt.dto.TaskDetailsWithTimelineDto;
import com.railse.hiring.workforcemgmt.dto.TaskEvent;
import com.railse.hiring.workforcemgmt.dto.TaskFetchByDateRequest;
import com.railse.hiring.workforcemgmt.dto.TaskFetchSmartRequest;
import com.railse.hiring.workforcemgmt.dto.TaskHistory;
import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.dto.UpdateTaskPriorityRequest;
import com.railse.hiring.workforcemgmt.dto.UpdateTaskRequest;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.repository.TaskMetaRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {
	private final TaskManagementService taskManagementService;
	private final TaskMetaRepository taskMetaRepository;


	   public TaskManagementController(TaskManagementService taskManagementService,TaskMetaRepository taskMetaRepository) {
	       this.taskManagementService = taskManagementService;
	       this.taskMetaRepository = taskMetaRepository;
	   }
	   
	   @GetMapping("/{id}")
	   public Response<?> getTaskById(@PathVariable Long id) {
		   TaskManagementDto task = taskManagementService.findTaskById(id);
		    List<TaskComment> comments = taskMetaRepository.getCommentsByTaskId(id);
		    List<TaskHistory> history = taskMetaRepository.getHistoryByTaskId(id);

		    List<TaskEvent> timeline = new ArrayList<>();

		    for (TaskComment comment : comments) {
		        TaskEvent event = new TaskEvent();
		        event.setTimestamp(comment.getTimestamp());
		        event.setType("COMMENT");
		        event.setMessage(comment.getComment());
		        timeline.add(event);
		    }

		    for (TaskHistory h : history) {
		        TaskEvent event = new TaskEvent();
		        event.setTimestamp(h.getTimestamp());
		        event.setType("HISTORY");
		        event.setMessage(h.getAction());
		        timeline.add(event);
		    }

		    
		    timeline.sort(Comparator.comparingLong(TaskEvent::getTimestamp));

		    TaskDetailsWithTimelineDto response = new TaskDetailsWithTimelineDto();
		    response.setTask(task);
		    response.setTimeline(timeline);

		    return new Response<>(response);
	   }

	   @PostMapping("/create")
	   public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {		    
	       return new Response<>(taskManagementService.createTasks(request));
	   }


	   @PostMapping("/update")
	   public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
	       return new Response<>(taskManagementService.updateTasks(request));
	   }


	   @PostMapping("/assign-by-ref")
	   public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
	       return new Response<>(taskManagementService.assignByReference(request));
	   }


	   @PostMapping("/fetch-by-date/v2")
	   public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
	       return new Response<>(taskManagementService.fetchTasksByDate(request));
	   }
	   
	   @PostMapping("/smart-daily")
	   public Response<List<TaskManagementDto>> getSmartTasks(@RequestBody TaskFetchSmartRequest request) {
	       return new Response<>(taskManagementService.getSmartDailyTasks(
	           request.getAssigneeIds(), request.getStartDate(), request.getEndDate()
	       ));
	   }
	   
	   @PutMapping("/update-priority")
	   public Response<String> updateTaskPriority(@RequestBody UpdateTaskPriorityRequest request) {
	       taskManagementService.updateTaskPriority(request);
	       return new Response<>("Priority updated successfully");
	   }

	   @GetMapping("/tasks/priority/HIGH")
	   public Response<List<TaskManagementDto>> getByHighPriority() {
	       return new Response<>(taskManagementService.getTasksByPriority(Priority.HIGH));
	   }
	   
	   @GetMapping("/tasks/priority/LOW")
	   public Response<List<TaskManagementDto>> getByLowPriority() {
		   return new Response<>(taskManagementService.getTasksByPriority(Priority.LOW));
	   }
	   
	   @GetMapping("/tasks/priority/MEDIUM")
	   public Response<List<TaskManagementDto>> getByMediumPriority() {
		   return new Response<>(taskManagementService.getTasksByPriority(Priority.MEDIUM));
	   }
	   
	   @PostMapping("/comment")
	   public Response<String> addComment(@RequestBody TaskComment comment) {
	       comment.setTimestamp(System.currentTimeMillis());
	       taskMetaRepository.saveComment(comment);
	       return new Response<>("Comment added successfully");
	   }

	   @GetMapping("/{id}/comments")
	   public Response<List<TaskComment>> getComments(@PathVariable Long id) {
	       return new Response<>(taskMetaRepository.getCommentsByTaskId(id));
	   }
	   
	  

}
