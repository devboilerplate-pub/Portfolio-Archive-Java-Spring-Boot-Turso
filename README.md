# Portfolio Project Archive (포트폴리오 아카이브)

**Portfolio Project Archive**는 'Creative Technical Archive'를 주제로 개발된 **Java + Spring Boot + Turso** 기반의 학습 및 템플릿 프로젝트입니다. 

이 프로젝트는 복잡한 프레임워크(React, Vue, JPA/Hibernate, Spring Security 등)에 의존하지 않고, 백엔드의 가장 기본적이고 순수한 데이터 흐름인 **`Controller → Service → Repository → Turso HTTP SQL`**의 구조를 직관적으로 이해할 수 있도록 설계되었습니다.

## ✨ 주요 특징

- **경량화된 스택**: 무거운 ORM(JPA) 대신 직접 SQL 쿼리를 작성하여 Turso DB와 통신합니다.
- **Edge Database 통합**: SQLite 기반의 글로벌 엣지 데이터베이스인 [Turso](https://turso.tech/)를 HTTP 파이프라인으로 연결하여 사용합니다.
- **순수 웹 기술**: 프론트엔드는 Spring Boot가 제공하는 템플릿 엔진(Thymeleaf)과 순수 HTML/CSS/JavaScript로만 구성되어 있습니다.
- **개발/운영 모드 분리**: 환경변수(DB 정보)가 없을 때는 메모리에서 기본(시드) 프로젝트 8개를 임시로 제공하여, 로컬 환경에서 즉시 실행하고 테스트해 볼 수 있습니다.

## 🛠 기술 스택

- **Backend**: Java 17, Spring Boot 3.4
- **Frontend**: Thymeleaf, HTML, CSS, Vanilla JavaScript
- **Database**: Turso (libSQL)
- **Build Tool**: Gradle

## 📂 아키텍처 및 폴더 구조

프로젝트는 직관적인 계층형 아키텍처(Layered Architecture)를 따릅니다.

- **`Controller`** (`src/main/java/com/example/archive/controller`): 클라이언트의 HTTP 웹 요청 및 API 요청을 처리합니다.
- **`Service`**: 비즈니스 로직 및 유즈케이스를 처리하고 조율합니다.
- **`Repository`**: 데이터베이스(Turso)와의 통신 및 SQL 실행을 담당합니다.
- **`Static Resources`** (`src/main/resources/static`): 정적 파일(CSS, JS, 이미지 등)을 보관합니다.
- **`Templates`** (`src/main/resources/templates`): 화면 렌더링을 위한 Thymeleaf HTML 파일들을 보관합니다.

## 🚀 시작하기

### 1. 사전 준비 (Prerequisites)
프로젝트를 실행하려면 환경 시스템에 Java 17 이상이 설치되어 있어야 합니다. 실제 Turso DB와 연동하려면 계정과 DB 설정이 필요합니다.

### 2. 환경 변수 설정
운영 환경 또는 실제 DB 연동을 위해서는 아래 환경 변수를 설정해야 합니다. (로컬 테스트용으로는 설정하지 않아도 메모리 기반으로 동작합니다.)

```bash
export TURSO_DATABASE_URL="libsql://your-db-your-org.turso.io"
export TURSO_AUTH_TOKEN="your-token"
```
> ⚠️ **주의**: 실제 운영 환경에서는 토큰 정보를 소스 코드나 텍스트 파일에 하드코딩하지 말고, 안전한 Secret 저장소나 환경 변수로 관리하세요.

### 3. 서버 실행
아래 명령어를 통해 Spring Boot 서버를 실행합니다.

```bash
./gradlew bootRun
```

### 4. 접속
실행이 완료되면 브라우저를 열고 `http://localhost:8080` 으로 접속하여 앱을 확인할 수 있습니다.

## 🗄 데이터베이스 스키마 (Turso 초기 SQL)

Turso DB 연동 시 초기 데이터를 구성하기 위한 DDL 쿼리입니다.

```sql
CREATE TABLE IF NOT EXISTS projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    title TEXT NOT NULL, 
    category TEXT NOT NULL, 
    description TEXT, 
    problem TEXT, 
    solution TEXT, 
    technology TEXT, 
    image TEXT, 
    github_url TEXT, 
    demo_url TEXT, 
    created_at TEXT
);

CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT, 
    name TEXT NOT NULL UNIQUE
);
```

## 🔌 API 명세

프론트엔드 통신 및 외부 연동을 위한 RESTful API를 제공합니다.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/projects` | 전체 프로젝트 목록 조회 |
| `GET` | `/api/projects/{id}` | 특정 프로젝트 상세 정보 조회 |
| `POST` | `/api/projects` | 신규 프로젝트 등록 |
| `PUT` | `/api/projects/{id}` | 기존 프로젝트 정보 수정 |
| `DELETE` | `/api/projects/{id}` | 프로젝트 삭제 |
| `GET` | `/api/categories` | 프로젝트 카테고리 목록 조회 |
