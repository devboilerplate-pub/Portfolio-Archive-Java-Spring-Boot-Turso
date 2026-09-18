# Portfolio Project Archive

Creative Technical Archive를 주제로 만든 Java + Spring Boot + Turso 학습 프로젝트입니다. React, Vue, JPA/Hibernate, Spring Security 없이 **Controller → Service → Repository → Turso HTTP SQL** 흐름을 그대로 보여줍니다.

## 실행

```bash
export TURSO_DATABASE_URL="libsql://your-db-your-org.turso.io"
export TURSO_AUTH_TOKEN="your-token"
./gradlew bootRun
```

브라우저에서 `http://localhost:8080`을 엽니다. 환경변수가 없는 로컬 미리보기에서는 8개 시드 프로젝트를 메모리에서 제공하며, 환경변수가 설정되면 Repository가 Turso libSQL HTTP pipeline을 사용합니다. 운영 환경에서는 반드시 두 환경변수를 Secret으로 주입하고 토큰을 파일에 기록하지 마세요.

## Turso 초기 SQL

```sql
CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, category TEXT NOT NULL, description TEXT, problem TEXT, solution TEXT, technology TEXT, image TEXT, github_url TEXT, demo_url TEXT, created_at TEXT);
CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE);
```

## API

- `GET /api/projects`
- `GET /api/projects/{id}`
- `GET /api/categories`
- `POST /api/projects`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`

## 구성

`src/main/java/com/example/archive/controller`는 HTTP 요청을 받고, `service`는 사용 사례를 조정하며, `repository`는 SQL과 Turso 연결을 담당합니다. `src/main/resources/static`에는 Spring Boot가 제공하는 최소 HTML/CSS/JavaScript가 있습니다.
