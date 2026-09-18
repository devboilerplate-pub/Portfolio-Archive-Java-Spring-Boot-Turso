package com.example.archive.service;

import com.example.archive.dto.ProjectRequest;
import com.example.archive.exception.ProjectNotFoundException;
import com.example.archive.model.Project;
import com.example.archive.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<Project> all() {
        return repository.findAll();
    }

    public Project one(long id) {
        return repository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    public Project create(ProjectRequest request) {
        return repository.save(request);
    }

    public Project edit(long id, ProjectRequest request) {
        return repository.update(id, request);
    }

    public void remove(long id) {
        repository.delete(id);
    }

    public boolean tursoConfigured() {
        return repository.isTursoConfigured();
    }
}
