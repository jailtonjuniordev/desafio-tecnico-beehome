package com.jjdev.beehome_tecnico.task.rest.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

import com.jjdev.beehome_tecnico.task.application.service.TaskService;
import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskResponseDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody TaskCreateRequestDTO request) {
        return new ResponseEntity<>(taskService.create(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) OffsetDateTime deadlineStart,
            @RequestParam(required = false) OffsetDateTime deadlineEnd,
            @PageableDefault(sort = "deadline", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return new ResponseEntity<>(taskService.list(status, title, deadlineStart, deadlineEnd, pageable), HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<TaskResponseDTO>> filter(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) OffsetDateTime deadlineStart,
            @RequestParam(required = false) OffsetDateTime deadlineEnd,
            @PageableDefault(sort = "deadline", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return new ResponseEntity<>(taskService.list(status, title, deadlineStart, deadlineEnd, pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable UUID id) {
        return new ResponseEntity<>(taskService.getById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody TaskUpdateRequestDTO request) {
        return new ResponseEntity<>(taskService.update(id, request), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
