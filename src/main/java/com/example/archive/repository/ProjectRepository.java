package com.example.archive.repository;

import com.example.archive.dto.ProjectRequest;
import com.example.archive.exception.ProjectNotFoundException;
import com.example.archive.model.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProjectRepository {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Long, Project> memory = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(8);
    private final String dbUrl;
    private final String token;

    public ProjectRepository(@Value("${turso.database.url:}") String dbUrl, @Value("${turso.auth.token:}") String token) {
        this.dbUrl = dbUrl == null ? "" : dbUrl;
        this.token = token == null ? "" : token;
        seed();
    }

    public boolean isTursoConfigured() {
        return !dbUrl.isBlank() && !token.isBlank();
    }

    public List<Project> findAll() {
        if (!isTursoConfigured()) {
            return memory.values().stream().sorted(Comparator.comparing(project -> project.id())).toList();
        }
        try {
            JsonNode rows = execute("SELECT id,title,category,description,problem,solution,technology,image,github_url,demo_url,created_at FROM projects ORDER BY id");
            List<Project> out = new ArrayList<>();
            for (JsonNode row : rows) {
                out.add(fromRow(row));
            }
            return out;
        } catch (Exception e) {
            return memory.values().stream().sorted(Comparator.comparing(project -> project.id())).toList();
        }
    }

    public Optional<Project> findById(long id) {
        if (!isTursoConfigured()) {
            return Optional.ofNullable(memory.get(id));
        }
        try {
            JsonNode rows = execute(
                    "SELECT id,title,category,description,problem,solution,technology,image,github_url,demo_url,created_at FROM projects WHERE id = ?",
                    List.of(arg(id))
            );
            if (rows.isArray() && !rows.isEmpty()) {
                return Optional.of(fromRow(rows.get(0)));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.ofNullable(memory.get(id));
        }
    }

    public Project save(ProjectRequest r) {
        long id = sequence.incrementAndGet();
        String date = LocalDate.now().toString();
        
        if (isTursoConfigured()) {
            try {
                JsonNode rows = execute(
                        "INSERT INTO projects(title,category,description,problem,solution,technology,image,github_url,demo_url,created_at) VALUES (?,?,?,?,?,?,?,?,?,?) RETURNING id",
                        List.of(
                                arg(r.title()), arg(r.category()), arg(r.description()),
                                arg(r.problem()), arg(r.solution()), arg(r.technology()),
                                arg(r.image()), arg(r.githubUrl()), arg(r.demoUrl()), arg(date)
                        )
                );
                if (rows.isArray() && !rows.isEmpty()) {
                    id = rows.get(0).get(0).asLong();
                    sequence.set(Math.max(sequence.get(), id));
                }
            } catch (Exception e) {
                // Fallback to local sequence id if DB insertion fails
            }
        }
        
        Project p = new Project(id, r.title(), r.category(), r.description(), r.problem(), r.solution(), r.technology(), r.image(), r.githubUrl(), r.demoUrl(), date);
        memory.put(id, p);
        return p;
    }

    public Project update(long id, ProjectRequest r) {
        Project old = findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        Project p = new Project(id, r.title(), r.category(), r.description(), r.problem(), r.solution(), r.technology(), r.image(), r.githubUrl(), r.demoUrl(), old.createdAt());
        if (isTursoConfigured()) {
            execute(
                    "UPDATE projects SET title=?, category=?, description=?, problem=?, solution=?, technology=?, image=?, github_url=?, demo_url=? WHERE id=?",
                    List.of(
                            arg(r.title()), arg(r.category()), arg(r.description()),
                            arg(r.problem()), arg(r.solution()), arg(r.technology()),
                            arg(r.image()), arg(r.githubUrl()), arg(r.demoUrl()), arg(id)
                    )
            );
        }
        memory.put(id, p);
        return p;
    }

    public void delete(long id) {
        if (isTursoConfigured()) {
            execute("DELETE FROM projects WHERE id=?", List.of(arg(id)));
        }
        memory.remove(id);
    }

    private Map<String, Object> arg(Object val) {
        if (val == null) {
            return Map.of("type", "null");
        }
        if (val instanceof Number) {
            return Map.of("type", "integer", "value", String.valueOf(val));
        }
        return Map.of("type", "text", "value", String.valueOf(val));
    }

    private String buildPipelineEndpoint(String url) {
        if (url == null || url.isBlank()) return "";
        String normalized = url.trim();
        if (normalized.startsWith("libsql://")) {
            normalized = "https://" + normalized.substring("libsql://".length());
        } else if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.endsWith("/v2/pipeline")) {
            normalized += "/v2/pipeline";
        }
        return normalized;
    }

    private JsonNode execute(String sql) {
        return execute(sql, List.of());
    }

    private JsonNode execute(String sql, List<Map<String, Object>> args) {
        try {
            String base = buildPipelineEndpoint(dbUrl);
            Map<String, Object> stmt = new HashMap<>();
            stmt.put("sql", sql);
            if (args != null && !args.isEmpty()) {
                stmt.put("args", args);
            }
            String body = mapper.writeValueAsString(Map.of(
                    "requests", List.of(
                            Map.of("type", "execute", "stmt", stmt),
                            Map.of("type", "close")
                    )
            ));
            HttpRequest req = HttpRequest.newBuilder(URI.create(base))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                throw new IllegalStateException("Turso SQL request failed with status " + resp.statusCode() + ": " + resp.body());
            }
            JsonNode result = mapper.readTree(resp.body()).path("results").get(0).path("response").path("result");
            return result.path("rows");
        } catch (Exception e) {
            throw new IllegalStateException("Turso SQL request failed", e);
        }
    }

    private Project fromRow(JsonNode r) {
        return new Project(
                r.get(0).asLong(),
                r.get(1).asText(),
                r.get(2).asText(),
                r.get(3).asText(),
                r.get(4).asText(),
                r.get(5).asText(),
                r.get(6).asText(),
                r.get(7).asText(),
                r.get(8).asText(),
                r.get(9).asText(),
                r.get(10).asText()
        );
    }

    private void seed() {
        String[] titles = {"Grid / Portfolio System", "Signal Image Tool", "Archive Index", "Quiet Commerce", "Field Notes", "Night Shift", "Open Source Atlas", "Form & Function"};
        String[] cats = {"FRONTEND", "AI / IMAGE", "FULL STACK", "WEB", "EDITORIAL", "EXPERIMENT", "OPEN SOURCE", "BRAND SYSTEM"};
        for (int i = 0; i < titles.length; i++) {
            memory.put((long) i + 1, new Project(
                    (long) i + 1,
                    titles[i],
                    cats[i],
                    "A considered interface for a complex digital workflow.",
                    "The signal was fragmented across tools and contexts.",
                    "A compact system that makes the invisible structure legible.",
                    i % 2 == 0 ? "Spring Boot · SQL · JavaScript" : "TypeScript · CSS · API",
                    i % 2 == 0 ? "/images/project-1.jpg" : "/images/project-2.jpg",
                    "https://github.com",
                    "https://example.com",
                    "202" + (4 + i % 3) + "-0" + (i + 1) + "-12"
            ));
        }
    }
}
