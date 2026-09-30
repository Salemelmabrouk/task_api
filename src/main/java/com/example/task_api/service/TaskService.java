package com.example.task_api.service;

import com.example.task_api.dto.CreateTaskRequest;
import com.example.task_api.dto.TaskResponse;
import com.example.task_api.dto.UpdateTaskStatusRequest;
import com.example.task_api.entity.TaskStatus;

import java.util.List;

public interface TaskService {
    TaskResponse create(CreateTaskRequest request);

    List<TaskResponse> findAll(TaskStatus status);

    TaskResponse getTask(Long id);

    TaskResponse updateStatus(Long id, UpdateTaskStatusRequest request);
}
