package com.railse.hiring.workforcemgmt.repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.railse.hiring.workforcemgmt.dto.TaskComment;
import com.railse.hiring.workforcemgmt.dto.TaskHistory;

@Repository
public class TaskMetaRepository {
	private final Map<Long, TaskComment> comments = new ConcurrentHashMap<>();
    private final Map<Long, TaskHistory> histories = new ConcurrentHashMap<>();
    private final AtomicLong commentId = new AtomicLong(1);
    private final AtomicLong historyId = new AtomicLong(1);

    public TaskComment saveComment(TaskComment comment) {
        comment.setId(commentId.getAndIncrement());
        comments.put(comment.getId(), comment);
        return comment;
    }

    public List<TaskComment> getCommentsByTaskId(Long taskId) {
        return comments.values().stream()
            .filter(c -> c.getTaskId().equals(taskId))
            .collect(Collectors.toList());
    }

    public TaskHistory saveHistory(TaskHistory history) {
        history.setId(historyId.getAndIncrement());
        histories.put(history.getId(), history);
        return history;
    }

    public List<TaskHistory> getHistoryByTaskId(Long taskId) {
        return histories.values().stream()
            .filter(h -> h.getTaskId().equals(taskId))
            .collect(Collectors.toList());
    }
}
