# AI 프롬프트 이력 - claude

## 대화 1: 수강신청 시스템 전체 구현

### 사용자 요청
대학교 수강신청 시스템의 전체 구현 계획을 제시하고, 해당 계획에 따라 구현을 요청함.

**핵심 요구사항:**
- Spring Boot 4.0.2 + Java 21 + H2 인메모리 DB
- 동시성 제어 (정원 1명, 100명 동시 요청 시 1명만 성공)
- 비즈니스 규칙: 18학점 제한, 시간표 충돌 방지, 중복 수강 방지
- 초기 데이터 동적 생성 (학과 12개, 교수 100+명, 강좌 500개, 학생 10,000명)
- CLAUDE.md 코딩 가이드라인 준수 (데이터 파일 금지, 토큰 배열 + 런타임 생성 필수)

**제시한 구현 계획 (12 Step):**
1. 의존성 추가 (JPA, H2, Validation, Swagger)
2. application.yml 설정
3. Domain Entity 6개 (Department, Professor, Course, CourseSchedule, Student, Enrollment)
4. Repository 계층 (CourseRepository에 PESSIMISTIC_WRITE 락)
5. DTO + ApiResponse 통일 응답 형식
6. HealthController (/health)
7. DataInitializer (ApplicationRunner, 배치 삽입)
8. 조회 API (courses, students, professors)
9. 수강신청/취소 API + 동시성 제어
10. GlobalExceptionHandler
11. 테스트 (동시성 + 비즈니스 규칙)
12. 문서화 (REQUIREMENTS.md, API.md)

### AI 답변 요약

계획에 따라 10개 태스크로 분리하여 순차적으로 구현 완료.

**구현 결과:**
- **27개 파일** 생성/수정
- **8개 테스트** 모두 통과
  - 동시성 테스트: 정원 1명, 100스레드 동시 요청 → 정확히 1명만 성공
  - 비즈니스 규칙 테스트 6개: 수강신청 성공, 정원 초과, 중복 수강, 학점 초과, 시간 충돌, 취소 후 재신청
  - contextLoads 1개
- **데이터 생성**: 학과 12개, 교수 106명, 강좌 500개, 시간표 1,000개, 학생 10,000명
- **API 엔드포인트 9개**: health, courses(목록/상세), enrollments(신청/취소), students(목록/상세/시간표), professors
- **서버 기동 후 검증**: health OK, 모든 API 정상 동작 확인

**구현 중 수정사항:**
- 강좌 생성 로직에서 변형 강좌 수를 3→4로 조정하여 480개→500개 달성

**프로젝트 구조:**
```
src/main/java/com/musinsa/course/
├── domain/          Department, Professor, Course, CourseSchedule, DayOfWeek, Student, Enrollment
├── repository/      6개 Repository (CourseRepository에 PESSIMISTIC_WRITE 락)
├── dto/             ApiResponse, CourseResponse, EnrollmentRequest/Response, StudentResponse, ProfessorResponse
├── exception/       6개 커스텀 예외 + GlobalExceptionHandler
├── service/         DataInitializer, CourseService, EnrollmentService
├── controller/      HealthController, CourseController, EnrollmentController, StudentController, ProfessorController
docs/                REQUIREMENTS.md, API.md
```

**실행 방법:**
```bash
./gradlew bootRun    # 서버 시작 (localhost:8080)
./gradlew test       # 전체 테스트
```

## 대화 2: 프롬프트 이력 정리 요청
- 사용자: 지금까지 대화한 프롬프트를 `prompts/prompts.md`에 정리 요청.
- claude: 대화 1(전체 구현)의 요청/답변을 요약하여 기록.

## 대화 3: 분할 커밋 메시지 추천 요청
- 사용자: 작업 내용 커밋 메시지 추천 요청. 분할 커밋으로 진행하되 커밋은 직접 수행.
- claude: 6개 분할 커밋 구조 제안 및 각 커밋에 해당하는 파일 목록 제공.
  1. `chore: Spring Boot 프로젝트 초기 설정 (JPA, H2, Swagger)` — 빌드/설정 파일
  2. `feat: 도메인 엔티티 및 리포지토리 구현` — domain/, repository/
  3. `feat: DTO, 예외 처리, 초기 데이터 생성 구현` — dto/, exception/, DataInitializer
  4. `feat: 수강신청/취소 API 및 조회 컨트롤러 구현 (비관적 락 동시성 제어)` — service/, controller/
  5. `test: 동시성 및 비즈니스 규칙 테스트 추가` — test/
  6. `docs: 요구사항, API 문서, README, 프롬프트 이력 작성` — docs/, README, prompts/

## 대화 4: 프롬프트 이력 업데이트 요청
- 사용자: 커밋 완료 후 프롬프트 이력 업데이트 요청. `# AI 프롬프트 이력 - claude` 하위에 작성하도록 지시.
- claude: 대화 2~4 내용을 claude 섹션에 추가.

## 대화 5: 개발 기능 점검 요청
- 사용자: subagents를 활용하여 현재까지 개발 내용 중 미비한 사항 점검 및 개선 사항 정리 요청
- claude: 5개의 subagents를 활용하여 기능 검증 후 수정 사항 제공
  1. CapacityExceededException 내 현재 수강인원 추가 안내
  2. 엣지 케이스 테스트 추가 - 존재하지 않는 학생, 강좌, 수강신청 id, 시간이 중복되는 경우 테스트 추가

---

# AI 대화 이력 - codex

### 대화 2: TODO 작성 요청
- 사용자: HELP.md를 보고 과제 해결용 개발 목록을 TODO로 정리 요청.
- codex: Spring Boot + Java 21 + Swagger 기준 TODO 목록 제시.

### 대화 3: 구현 상태 검증 요청
- 사용자: 코드 검증 및 빠진 구현 목록 확인 요청.
- codex: 코드 전수 확인 후 누락사항 보고 (학생 동시 신청 락 없음, 문서 부족, README 부재 등) 및 다음 단계 제안.

### 대화 4: README 작성
- 사용자: README 먼저 작성 지시.
- codex: `README.md` 생성(빌드/실행, 포트, Swagger, H2 정보 포함).

### 대화 5: API 문서 보강
- 사용자: API 문서 보강 지시.
- codex: `docs/API.md`에 공통 응답, 파라미터, 예시, 에러코드 추가.

### 대화 6: 동시성 범위 보완(학생 기준)
- 사용자: AGENTS.md 참고 여부 확인 후 동시성 보완 지시.
- codex: AGENTS.md 규칙 확인, 학생 행 비관적 락 추가(`StudentRepository.findByIdWithLock`, `EnrollmentService`에서 사용) 및 `docs/REQUIREMENTS.md`에 전략 기록. 작업 계획/완료 상태 갱신.

### 대화 7: 프롬프트 기록 요청
- 사용자: 지금까지 대화 정리하여 prompts.md에 기록 지시.
- codex: 본 섹션 추가.

### 대화 8: Student/Professor 서비스 레이어 도입
- 사용자: Controller가 Repository를 직접 호출하는 부분을 서비스 레이어로 분리 요청(우선 Student).
- codex: `StudentService` 신설, `StudentController` 서비스 의존으로 리팩터링. 이어서 동일 기준으로 `ProfessorService` 신설 및 `ProfessorController` 리팩터링.

### 대화 9: 테스트 실패 원인 파악
- 사용자: 시간 충돌 테스트 2건 실패 원인 분석 요청.
- codex: 예외 메시지 오타로 인한 `hasMessageContaining` 불일치가 원인임을 설명(로직은 정상).

### 대화 10: 정렬 파라미터 오류 대응
- 사용자: `sort=DESC` 요청 시 정렬 오류 발생 보고.
- codex: Student/Professor 목록 API에서 방향만 온 경우 기본 컬럼 `id`로 정렬하도록 보정 로직 추가; API 문서에 정렬 규칙 반영.

### 대화 11: Swagger 문서 보정
- 사용자: Swagger 문서 알맞게 수정 지시.
- codex: `docs/API.md` 정렬 파라미터 설명 보강(방향만 전달 시 id 기준 정렬 명시).

### 대화 12: Swagger 정렬 예시 추가
- 사용자: Swagger 문서 예시가 올바르지 않으니 수정 요청.
- codex: 학생/교수 목록 API에 허용 정렬 컬럼 명시 및 요청 예시 추가, 잘못된 컬럼/방향만 전달 시 보정 규칙 문서화.

### 대화 13: Swagger 예시값 혼합 문제
- 사용자: 성공 응답 예시에 에러 필드가 함께 노출되는 문제 제기.
- codex: `ApiResponse` 필드 예시 제거로 성공/에러 혼합 표시 문제 해소.

### 대화 14: 학생 단건 조회 404 예시 추가
- 사용자: 404 응답 예시를 별도로 추가 요청.
- codex: `StudentController`에 200/404 `@ApiResponses`와 404 JSON 예시 추가.

### 대화 15: 추가 테스트 커버리지 요청
- 사용자: 빠진 엣지 케이스 검토 요청.
- codex: 부족 케이스 제안 후, 시간 충돌 정상/경계, 학점 경계, 중복 취소, 동일 학생 동시 신청 등 테스트 추가.

### 대화 16: 신규 테스트 실패 원인 분석
- 사용자: 새로 추가한 동시성/학점 경계 테스트 실패 보고.
- codex: (1) 충돌 없는 스케줄이라 기대값 잘못 설정, (2) 학점 합계가 18을 넘지 않아 예외가 발생하지 않는 상황임을 설명. 원하는 시나리오(충돌 유발 또는 학점 초과)로 테스트 수정 필요 안내.
