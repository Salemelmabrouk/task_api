package com.example.task_api.service;


import com.example.task_api.dto.CreateTaskRequest;
import com.example.task_api.dto.TaskResponse;
import com.example.task_api.dto.UpdateTaskStatusRequest;
import com.example.task_api.entity.Task;
import com.example.task_api.entity.TaskStatus;
import com.example.task_api.exception.InvalidStatusTransitionException;
import com.example.task_api.exception.TaskNotFoundException;
import com.example.task_api.repository.TaskRepository;
import com.example.task_api.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task(request.title(), request.description());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    public List<TaskResponse> findAll(TaskStatus status) {
        List<Task> tasks = status == null
                ? taskRepository.findAll()
                : taskRepository.findByStatus(status);

        return tasks.stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public TaskResponse getTask(Long id) {
        return TaskResponse.from(findTaskEntity(id));
    }

    @Override
    @Transactional
    public TaskResponse updateStatus(
            Long id,
            UpdateTaskStatusRequest request
    ) {
        Task task = findTaskEntity(id);
        TaskStatus nextStatus = request.status();

        if (!task.getStatus().canTransitionTo(nextStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change task status from "
                            + task.getStatus()
                            + " to "
                            + nextStatus
            );
        }

        task.changeStatus(nextStatus);

        return TaskResponse.from(task);
    }

    private Task findTaskEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "Task " + id + " was not found"
                        )
                );
    }
}

