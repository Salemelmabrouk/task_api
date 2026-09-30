package com.example.task_api.entity;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    public boolean canTransitionTo(TaskStatus nextStatus) {
        return (this == TODO && nextStatus == IN_PROGRESS)
                || (this == IN_PROGRESS && nextStatus == DONE);
    }
}
