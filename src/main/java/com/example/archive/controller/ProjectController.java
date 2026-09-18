package com.example.archive.controller;

import com.example.archive.dto.ProjectRequest;
import com.example.archive.model.Project;
import com.example.archive.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectController {
    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping("/projects")
    public List<Project> all() {
        return service.all();
    }

    @GetMapping("/projects/{id}")
    public Project one(@PathVariable long id) {
        return service.one(id);
    }

    @PostMapping("/projects")
    public Project create(@Valid @RequestBody ProjectRequest request) {
        return service.create(request);
    }

    @PutMapping("/projects/{id}")
    public Project edit(@PathVariable long id, @Valid @RequestBody ProjectRequest request) {
        return service.edit(id, request);
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.remove(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return service.all().stream().map(project -> project.category()).distinct().sorted().toList();
    }

    @GetMapping("/status")
    public java.util.Map<String, Object> status() {
        return java.util.Map.of("database", service.tursoConfigured() ? "TURSO" : "PREVIEW MEMORY SEED", "projects", service.all().size());
    }
}
