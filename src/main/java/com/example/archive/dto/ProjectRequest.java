package com.example.archive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must be at most 100 characters")
        String category,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @Size(max = 2000, message = "Problem must be at most 2000 characters")
        String problem,

        @Size(max = 2000, message = "Solution must be at most 2000 characters")
        String solution,

        @Size(max = 200, message = "Technology must be at most 200 characters")
        String technology,

        @Size(max = 500, message = "Image URL must be at most 500 characters")
        String image,

        @Size(max = 500, message = "GitHub URL must be at most 500 characters")
        String githubUrl,

        @Size(max = 500, message = "Demo URL must be at most 500 characters")
        String demoUrl
) {}
