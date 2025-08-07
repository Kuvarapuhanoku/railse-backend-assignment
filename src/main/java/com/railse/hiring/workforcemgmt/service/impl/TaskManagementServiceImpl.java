package com.railse.hiring.workforcemgmt.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.AssignByReferenceRequest;
import com.railse.hiring.workforcemgmt.dto.TaskCreateRequest;
import com.railse.hiring.workforcemgmt.dto.TaskFetchByDateRequest;
import com.railse.hiring.workforcemgmt.dto.TaskHistory;
import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.dto.UpdateTaskPriorityRequest;
import com.railse.hiring.workforcemgmt.dto.UpdateTaskRequest;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskMetaRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;


@Service
public class TaskManagementServiceImpl implements TaskManagementService {
	private final TaskRepository taskRepository;
	private final ITaskManagementMapper taskMapper;
	private final TaskMetaRepository taskMetaRepository;


	   public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper, TaskMetaRepository taskMetaRepository) {
	       this.taskRepository = taskRepository;
	       this.taskMapper = taskMapper;
	       this.taskMetaRepository = taskMetaRepository;
	   }

	   
	@Override
	public List<TaskManagementDto> createTasks(TaskCreateRequest request) {
		 List<TaskManagement> createdTasks = new ArrayList<>();
		 long timestamp = System.currentTimeMillis();
		 
	       for (TaskCreateRequest.RequestItem item : request.getRequests()) {
	           TaskManagement newTask = new TaskManagement();
	           newTask.setReferenceId(item.getReferenceId());
	           newTask.setReferenceType(item.getReferenceType());
	           newTask.setTask(item.getTask());
	           newTask.setAssigneeId(item.getAssigneeId());
	           newTask.setPriority(item.getPriority());
	           newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
	           newTask.setStatus(TaskStatus.ASSIGNED);
	           newTask.setDescription("New task created.");
	           createdTasks.add(taskRepository.save(newTask));
	           
	           TaskHistory history = new TaskHistory();
	           history.setTaskId(newTask.getId());
	           history.setAction("Task created for assignee " + item.getAssigneeId());
	           history.setTimestamp(timestamp);
	           taskMetaRepository.saveHistory(history);
	       }
	       return taskMapper.modelListToDtoList(createdTasks);
	}


	@Override
	public List<TaskManagementDto> updateTasks(UpdateTaskRequest request) {
		List<TaskManagement> updatedTasks = new ArrayList<>();
	       for (UpdateTaskRequest.RequestItem item : request.getRequests()) {
	           TaskManagement task = taskRepository.findById(item.getTaskId())
	                   .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));


	           if (item.getTaskStatus() != null && task.getStatus() != item.getTaskStatus()) {
	        	    String action = "Status changed from " + task.getStatus() + " to " + item.getTaskStatus();
	        	    TaskHistory history = new TaskHistory();
	        	    history.setTaskId(task.getId());
	        	    history.setAction(action);
	        	    history.setTimestamp(System.currentTimeMillis());
	        	    taskMetaRepository.saveHistory(history);

	        	    task.setStatus(item.getTaskStatus());
	        	}
	        	if (item.getDescription() != null && !item.getDescription().equals(task.getDescription())) {
	        	    String action = "Description changed.";
	        	    TaskHistory history = new TaskHistory();
	        	    history.setTaskId(task.getId());
	        	    history.setAction(action);
	        	    history.setTimestamp(System.currentTimeMillis());
	        	    taskMetaRepository.saveHistory(history);

	        	    task.setDescription(item.getDescription());
	        	}

	           updatedTasks.add(taskRepository.save(task));
	       }
	       return taskMapper.modelListToDtoList(updatedTasks);

	}


	@Override
	public String assignByReference(AssignByReferenceRequest request) {
		 List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
		    List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(
		        request.getReferenceId(), request.getReferenceType());

		    long timestamp = System.currentTimeMillis();

		    for (Task taskType : applicableTasks) {
		        List<TaskManagement> tasksOfType = existingTasks.stream()
		            .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
		            .collect(Collectors.toList());

		        if (!tasksOfType.isEmpty()) {
		            TaskManagement toAssign = tasksOfType.get(0);
		            toAssign.setAssigneeId(request.getAssigneeId());
		            taskRepository.save(toAssign);

		            TaskHistory history = new TaskHistory();
		            history.setTaskId(toAssign.getId());
		            history.setAction("Task reassigned by reference " + request.getReferenceId() + " to assignee " + request.getAssigneeId());
		            history.setTimestamp(timestamp);
		            taskMetaRepository.saveHistory(history);

		            for (int i = 1; i < tasksOfType.size(); i++) {
		                TaskManagement toCancel = tasksOfType.get(i);
		                toCancel.setStatus(TaskStatus.CANCELLED);
		                taskRepository.save(toCancel);

		                TaskHistory cancelHistory = new TaskHistory();
		                cancelHistory.setTaskId(toCancel.getId());
		                cancelHistory.setAction("Task auto-cancelled due to reassignment");
		                cancelHistory.setTimestamp(timestamp);
		                taskMetaRepository.saveHistory(cancelHistory);
		            }
		        } else {
		            TaskManagement newTask = new TaskManagement();
		            newTask.setReferenceId(request.getReferenceId());
		            newTask.setReferenceType(request.getReferenceType());
		            newTask.setTask(taskType);
		            newTask.setAssigneeId(request.getAssigneeId());
		            newTask.setStatus(TaskStatus.ASSIGNED);
		            taskRepository.save(newTask);

		            TaskHistory history = new TaskHistory();
		            history.setTaskId(newTask.getId());
		            history.setAction("Task created by reference assignment for assignee " + request.getAssigneeId());
		            history.setTimestamp(timestamp);
		            taskMetaRepository.saveHistory(history);
		        }
		    }

		    return "Tasks assigned successfully for reference " + request.getReferenceId();

	}


	@Override
	public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
		 List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());


	       List<TaskManagement> filteredTasks = tasks.stream()
	               .filter(task ->
	                task.getStatus() != TaskStatus.CANCELLED &&
	               	task.getTaskDeadlineTime() != null &&
	               	task.getTaskDeadlineTime() >= request.getStartDate() &&
	               	task.getTaskDeadlineTime() <= request.getEndDate()
	               	)
	               .collect(Collectors.toList());


	       return taskMapper.modelListToDtoList(filteredTasks);
	}


	@Override
	public TaskManagementDto findTaskById(Long id) {
		 TaskManagement task = taskRepository.findById(id)
	               .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
	       return taskMapper.modelToDto(task);

	}


	@Override
	public List<TaskManagementDto> getSmartDailyTasks(List<Long> assigneeIds,  long startDate, long endDate) {
		return taskRepository.findAll().stream()
		        .filter(task -> assigneeIds.contains(task.getAssigneeId()))
		        .filter(task ->
		            task.getStatus() != TaskStatus.CANCELLED &&
		            (
		                (task.getTaskDeadlineTime() >= startDate && task.getTaskDeadlineTime() <= endDate) ||
		                (task.getTaskDeadlineTime() < startDate && task.getStatus() != TaskStatus.COMPLETED)
		            )
		        )
		        .map(taskMapper::modelToDto)
		        .collect(Collectors.toList());
	}


	@Override
	public void updateTaskPriority(UpdateTaskPriorityRequest request) {
		 TaskManagement task = taskRepository.findById(request.getTaskId())
			        .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

		 Priority oldPriority = task.getPriority();
		 task.setPriority(request.getPriority());
		 taskRepository.save(task);	
		 
		 TaskHistory history = new TaskHistory();
		 history.setTaskId(task.getId());
		 history.setAction("Priority changed from " + oldPriority + " to " + request.getPriority());
		 history.setTimestamp(System.currentTimeMillis());
		 taskMetaRepository.saveHistory(history);
	}	


	@Override
	public List<TaskManagementDto> getTasksByPriority(Priority priority) {
		 List<TaskManagement> tasks = taskRepository.findAll().stream()
			        .filter(task -> task.getPriority() == priority)
			        .collect(Collectors.toList());

			    return taskMapper.modelListToDtoList(tasks);
	}
	


	   
}
