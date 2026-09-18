# 자산(Assets) 관리 내역

이 문서는 프로젝트 내에서 외부 링크 형태로 사용되던 미디어(이미지, 영상 등) 자산들을 로컬 프로젝트 내부로 다운로드하여 관리하게 된 내역을 기록합니다.

## 1. 외부 이미지 로컬화 (Localizing External Images)

프로젝트의 초기 상태에서는 보일러플레이트 테스트 및 초기 데이터(`seed()`)용 썸네일 이미지를 Unsplash의 외부 링크(CDN)로 불러오고 있었습니다. 
하지만 외부 링크 사용 시 다음과 같은 문제가 발생할 수 있어, 이를 다운로드하여 로컬 정적 자산으로 포함시켰습니다.

- 외부 서비스(Unsplash)의 트래픽 제한이나 이미지 삭제 시 엑스박스가 뜨는 문제
- 오프라인 환경이나 제한된 네트워크망 내에서 프로젝트 실행 시 이미지를 볼 수 없는 문제
- 외부 도메인에 대한 추가적인 HTTP 커넥션 및 DNS 룩업으로 인한 로딩 지연

### 다운로드 및 변경 내역

| 변경 전 외부 URL (Unsplash) | 로컬 저장 경로 | 사용 위치 |
| --- | --- | --- |
| `https://images.unsplash.com/photo-1558655146-d09347e92766...` | `src/main/resources/static/images/project-1.jpg` | `ProjectRepository.java`의 `seed()` |
| `https://images.unsplash.com/photo-1519608487953-e999c86e7455...` | `src/main/resources/static/images/project-2.jpg` | `ProjectRepository.java`의 `seed()` |

## 2. 코드 수정 내역

`com.example.archive.repository.ProjectRepository` 클래스의 `seed()` 메서드 내부 삼항 연산자가 수정되었습니다.

**변경 전:**
```java
i % 2 == 0 ? "https://images.unsplash.com/photo-1558655146-d09347e92766?auto=format&fit=crop&w=1200&q=80" : "https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=1200&q=80",
```

**변경 후:**
```java
i % 2 == 0 ? "/images/project-1.jpg" : "/images/project-2.jpg",
```

이를 통해 애플리케이션 시작 시 로드되는 더미 데이터들이 모두 빠르고 안전한 로컬 이미지(`/images/project-1.jpg`, `/images/project-2.jpg`)를 참조하도록 개선되었습니다.
