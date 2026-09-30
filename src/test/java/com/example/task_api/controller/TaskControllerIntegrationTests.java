package com.example.task_api.controller;

import com.example.task_api.entity.Task;
import com.example.task_api.entity.TaskStatus;
import com.example.task_api.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void clearDatabase() {
        taskRepository.deleteAll();
    }

    // Required test 1: valid creation
    @Test
    void createsTaskSuccessfullyWithTodoStatus() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Learn Spring Boot",
                                  "description": "Prepare for the technical test"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Learn Spring Boot"))
                .andExpect(jsonPath("$.description").value("Prepare for the technical test"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    // Required test 2: invalid title
    @Test
    void rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "   ",
                                  "description": "Invalid task"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejectsTitleLongerThan120CharactersAfterTrim() throws Exception {
        String title = " " + "a".repeat(121) + " ";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsTitleWithExactly120CharactersAfterTrim() throws Exception {
        String title = " " + "a".repeat(120) + " ";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("a".repeat(120)));
    }

    @Test
    void listsAllTasks() throws Exception {
        taskRepository.save(new Task("First", null));
        taskRepository.save(new Task("Second", null));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void filtersTasksByStatus() throws Exception {
        Task inProgress = taskRepository.save(new Task("In progress", null));
        inProgress.changeStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(inProgress);
        taskRepository.save(new Task("To do", null));

        mockMvc.perform(get("/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("To do"))
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    void rejectsUnknownStatusFilter() throws Exception {
        mockMvc.perform(get("/tasks").param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // Required test 3: forbidden transition
    @Test
    void rejectsForbiddenTodoToDoneTransition() throws Exception {
        Task task = taskRepository.save(new Task("Complete the API", null));

        mockMvc.perform(patch("/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DONE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void allowsCompleteStatusLifecycle() throws Exception {
        Task task = taskRepository.save(new Task("Lifecycle", null));

        mockMvc.perform(patch("/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void rejectsSelfTransition() throws Exception {
        Task task = taskRepository.save(new Task("Self transition", null));

        mockMvc.perform(patch("/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TODO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsBackwardTransition() throws Exception {
        Task task = taskRepository.save(new Task("Backward transition", null));
        task.changeStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        mockMvc.perform(patch("/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TODO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsNotFoundForUnknownTask() throws Exception {
        mockMvc.perform(patch("/tasks/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsUnknownStatusInUpdate() throws Exception {
        Task task = taskRepository.save(new Task("Unknown status", null));

        mockMvc.perform(patch("/tasks/{id}/status", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
