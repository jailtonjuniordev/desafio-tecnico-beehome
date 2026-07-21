package com.jjdev.beehome_tecnico.task.rest.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jjdev.beehome_tecnico.shared.dto.ErroResponseDTO;
import com.jjdev.beehome_tecnico.shared.infrastructure.config.OpenApiConfig;
import com.jjdev.beehome_tecnico.task.application.service.TaskService;
import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskResponseDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task CRUD, filters and pagination")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create task", description = "Creates a task for the authenticated user (assignedTo from JWT). Title must be unique per user. Deadline cannot be in the past.")
    @ApiResponse(responseCode = "201", description = "Task created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed or deadline in the past",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Task title already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody TaskCreateRequestDTO request) {
        return new ResponseEntity<>(taskService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List tasks", description = "Lists tasks owned by the authenticated user with pagination and optional filters (status, title LIKE, deadline range). Default sort is deadline ascending.")
    @ApiResponse(responseCode = "200", description = "Page of tasks", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "400", description = "Invalid deadline range (deadlineStart after deadlineEnd)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<Page<TaskResponseDTO>> list(
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(description = "Filter by title (partial, case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Deadline range start (inclusive)")
            @RequestParam(required = false) OffsetDateTime deadlineStart,
            @Parameter(description = "Deadline range end (inclusive)")
            @RequestParam(required = false) OffsetDateTime deadlineEnd,
            @PageableDefault(sort = "deadline", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return new ResponseEntity<>(taskService.list(status, title, deadlineStart, deadlineEnd, pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id", description = "Returns a task only if it belongs to the authenticated user. Missing or foreign tasks return 404.")
    @ApiResponse(responseCode = "200", description = "Task found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(taskService.getById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates a task owned by the authenticated user. Title must remain unique per user. Deadline cannot be in the past.")
    @ApiResponse(responseCode = "200", description = "Task updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TaskResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed or deadline in the past",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Task title already exists",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<TaskResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequestDTO request
    ) {
        return new ResponseEntity<>(taskService.update(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Permanently deletes a task owned by the authenticated user (hard delete).")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Task not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
