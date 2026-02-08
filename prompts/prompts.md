# AI 프롬프트 이력

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

## AI 대화 이력 - codex

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
