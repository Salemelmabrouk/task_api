package com.example.task_api.controller;

import com.example.task_api.dto.CreateTaskRequest;
import com.example.task_api.dto.TaskResponse;
import com.example.task_api.dto.UpdateTaskStatusRequest;
import com.example.task_api.entity.TaskStatus;
import com.example.task_api.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.create(request);
        URI location = URI.create("/tasks/" + task.id());
        return ResponseEntity.created(location).body(task);
    }

    @GetMapping
    public List<TaskResponse> findAll(@RequestParam(required = false) TaskStatus status) {
        return taskService.findAll(status);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable @Positive Long id) {
        return taskService.getTask(id);
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return taskService.updateStatus(id, request);
    }
}
