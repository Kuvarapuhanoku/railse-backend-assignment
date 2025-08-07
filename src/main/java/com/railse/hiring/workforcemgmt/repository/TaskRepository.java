package com.railse.hiring.workforcemgmt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.railse.hiring.workforcemgmt.model.TaskManagement;

@Repository
public interface TaskRepository {
	Optional<TaskManagement> findById(Long id);
	   TaskManagement save(TaskManagement task);
	   List<TaskManagement> findAll();
	   List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, com.railse.hiring.workforcemgmt.common.model.enums.ReferenceType referenceType);
	   List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds);
}
