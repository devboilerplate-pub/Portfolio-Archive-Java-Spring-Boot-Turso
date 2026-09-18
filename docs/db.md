# Turso Database 연동 아키텍처 및 구현 분석

이 문서는 본 프로젝트(`java portfolio-archive`)에서 데이터베이스(Turso/SQLite)를 어떻게 연동하고 관리하는지에 대한 정밀 분석 문서입니다.

## 1. 개요 (Overview)

본 프로젝트는 전통적인 JDBC(Java Database Connectivity) 드라이버나 Spring Data JPA(Hibernate)를 **사용하지 않습니다.** 대신, **Turso의 HTTP API(Pipeline API)**를 이용하여 직접 데이터베이스와 통신하는 경량화된 방식을 채택하고 있습니다. 

이는 서버리스 환경이나 엣지 환경에 최적화된 Turso의 특성을 반영하여 드라이버 의존성을 줄이고, HTTP 요청만으로 SQL을 실행할 수 있도록 설계된 것입니다.

## 2. 설정 및 초기화 (Configuration)

Turso 연결 정보는 `application.properties` 및 환경 변수를 통해 주입됩니다.
- `turso.database.url` (환경변수: `TURSO_DATABASE_URL`)
- `turso.auth.token` (환경변수: `TURSO_AUTH_TOKEN`)

`ProjectRepository.java`의 생성자에서 이 값들을 주입받으며, `isTursoConfigured()` 메서드를 통해 두 값이 모두 존재하는지 확인하여 DB 활성화 여부를 동적으로 결정합니다.

## 3. 통신 프로토콜: HTTP Pipeline API

데이터베이스 쿼리는 `execute(String sql, List<Map<String, Object>> args)` 내부에서 순수 HTTP 요청으로 처리됩니다.

1. **엔드포인트 정규화 (`buildPipelineEndpoint`)**
   - 주입받은 `libsql://` 프로토콜을 `https://`로 변환합니다.
   - URL 끝에 `/v2/pipeline`을 붙여 Turso의 Pipeline API 엔드포인트를 구성합니다.
2. **파라미터 바인딩 (`arg`)**
   - SQL Injection을 방지하기 위해 parameterized query를 지원합니다.
   - `arg()` 메서드를 통해 Java 타입을 Turso API 규격인 `{"type": "text", "value": "..."}` 형태의 JSON 객체로 변환합니다.
3. **요청 및 응답 파싱**
   - Java 11+ 내장 `HttpClient`를 사용하여 POST 요청을 보냅니다.
   - `ObjectMapper`(Jackson)를 사용해 응답 JSON을 수동으로 파싱(`JsonNode`)합니다.
   - `fromRow()` 메서드를 통해 JSON 배열을 `Project` 레코드 객체로 매핑합니다.

## 4. 메모리 기반 롤백/폴백 (Fallback Mechanism)

이 저장소 패턴의 가장 큰 특징 중 하나는 **우아한 성능 저하(Graceful Degradation)** 메커니즘입니다.

- **조건적 실행**: 환경 변수가 설정되지 않아 `isTursoConfigured()`가 `false`를 반환하면, 즉시 `ConcurrentHashMap` 기반의 메모리 저장소로 폴백합니다.
- **예외 처리 시 폴백**: `findAll()`이나 `findById()` 메서드는 HTTP 요청 중 예외(네트워크 오류, 인증 실패 등)가 발생하면 `catch` 블록에서 메모리 저장소의 데이터를 반환합니다.
- **초기 시드 데이터 (`seed()`)**: 애플리케이션 시작 시 `seed()` 메서드가 호출되어 8개의 더미 포트폴리오 데이터를 메모리에 로드합니다. 이를 통해 DB 없이도 UI 및 클라이언트 렌더링 테스트가 완벽하게 가능합니다.

## 5. 한계점 및 잠재적 이슈 (Potential Limitations)

현재 구현 방식은 가볍고 유연하지만, 다음과 같은 구조적 한계점과 고려 사항이 존재합니다.

1. **PK(Primary Key) 동기화 전략 (해결됨)**
   - 초기에는 DB의 `AUTOINCREMENT`와 로컬 메모리의 `AtomicLong sequence` 채번이 어긋나 ID 불일치 버그가 발생할 수 있는 구조였습니다.
   - 이를 해결하기 위해 `save()` 메서드에서 `INSERT` 쿼리 실행 시 `RETURNING id` 구문을 사용하여 Turso가 실제로 자동 채번한 ID를 응답받도록 개선되었습니다.
   - 반환받은 ID를 파싱하여 로컬 `Project` 객체에 세팅하고, `sequence.set(Math.max(sequence.get(), id))`를 통해 메모리 저장소의 시퀀스도 함께 최신화함으로써 PK 불일치 이슈를 완벽하게 해결했습니다.
2. **트랜잭션(Transaction) 부재**
   - 복잡한 비즈니스 로직(예: 여러 테이블에 걸친 쓰기)이 필요한 경우, 현재의 Pipeline API 구조만으로는 롤백(Rollback)이나 원자성(Atomicity)을 보장하기 어렵습니다.
3. **Connection Pooling 및 성능**
   - 매 쿼리마다 새로운 `HttpClient.newHttpClient()`를 생성하여 전송(`send()`)하고 있습니다. (L162)
   - 이는 매번 새로운 HTTP 클라이언트 객체를 생성하므로 리소스 낭비가 발생할 수 있습니다. `HttpClient`를 전역 싱글톤 멤버 변수로 재사용하도록 리팩토링하는 것이 성능 측면에서 권장됩니다.
