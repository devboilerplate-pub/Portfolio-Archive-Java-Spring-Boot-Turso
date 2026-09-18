package com.example.archive.model;

public record Project(Long id, String title, String category, String description, String problem,
                      String solution, String technology, String image, String githubUrl,
                      String demoUrl, String createdAt) {}
