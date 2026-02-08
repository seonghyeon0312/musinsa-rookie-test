# 수강신청 시스템 요구사항 및 설계 결정

## 1. 동시성 제어 전략

### 선택한 방법: 비관적 락 (Pessimistic Locking - PESSIMISTIC_WRITE)

### 선택 근거
- **단일 서버 환경**에서 가장 확실한 정합성 보장
- H2 인메모리 DB는 행 수준 락을 지원하며, JPA의 `@Lock(LockModeType.PESSIMISTIC_WRITE)`과 완벽히 호환
- 수강신청은 **정원 초과가 절대 허용되지 않는** 시나리오로, 낙관적 락의 재시도 로직보다 비관적 락의 "선착순 처리"가 더 적합
- 짧은 개발 시간 내에 가장 확실한 정합성을 보장하기 위해서는 성능이 약간 떨어지더라도 확실한 정합성을 보장하는 것이 서버 안정성, 서비스 신뢰성에 좋다고 생각하여 비관적 락으로 제어

### 구현 방식
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Course c WHERE c.id = :id")
Optional<Course> findByIdWithLock(@Param("id") Long id);
```
- 수강신청 시 `Course` 엔티티를 `PESSIMISTIC_WRITE` 락으로 조회
- 락을 획득한 트랜잭션만 정원 체크 + 등록 처리 수행
- 다른 트랜잭션은 락 해제 시까지 대기 후 순차 처리

#### 동일 학생 동시 신청 직렬화
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Student s WHERE s.id = :id")
Optional<Student> findByIdWithLock(@Param("id") Long id);
```
- 학점 합산 및 시간표 충돌 검증이 동일 학생의 동시 신청에서 깨지지 않도록 학생 행에 비관적 락 적용
- 순서: 강좌 락 → 학생 락으로 일관되게 획득해 교착 상태를 예방

### 트레이드오프 분석

| 항목 | 비관적 락 (선택) | 낙관적 락 | 분산 락 (Redis) |
|------|-----------------|----------|----------------|
| 정합성 | 확실 | 재시도 필요 | 확실 |
| 성능 | 락 대기 발생 | 충돌 적으면 유리 | 네트워크 비용 |
| 복잡도 | 낮음 | 중간 (재시도 로직) | 높음 (인프라 필요) |
| 적합 시나리오 | 충돌 빈번, 단일 서버 | 충돌 적음 | 다중 서버 |

### 검증 결과
- **정원 1명, 동시 요청 100명 -> 정확히 1명만 성공** (테스트 통과)

---

## 2. 시간표 구조 정의

### 테이블 설계
- `CourseSchedule` 별도 테이블로 분리
- 하나의 강좌가 여러 시간대를 가질 수 있음 (예: 월수 09:00-10:30)

### 요일/시간 표현 방식
- 요일: `DayOfWeek` enum (`MON`, `TUE`, `WED`, `THU`, `FRI`)
- 시간: `LocalTime` (09:00 ~ 18:00 범위)
- 수업 시간: 90분 단위 (1.5시간)

### 충돌 판단 로직
```
두 시간표 A, B가 충돌하는 조건:
1. 같은 요일 (A.dayOfWeek == B.dayOfWeek)
2. 시간이 겹침 (A.startTime < B.endTime AND B.startTime < A.endTime)
```

---

## 3. 데이터 생성 전략

### 생성 규모
| 항목 | 수량 |
|------|------|
| 학과 | 12개 |
| 교수 | 약 108명 |
| 강좌 | 500개 |
| 시간표 | 1,000개 (강좌당 2개) |
| 학생 | 10,000명 |

### 현실적 데이터 생성
- 성(20종) + 이름(30종) 토큰 조합으로 한국어 이름 생성
- 학과별 실제 전공 과목명 + 접미사(기초/심화/응용/실습/특론) 조합
- 학번: `2024XXXXX` 형식
- 학년: 1~4학년 랜덤

### 성능 최적화
- `saveAll()` 배치 삽입 사용
- Hibernate `jdbc.batch_size: 50` 설정
- `order_inserts: true`, `order_updates: true` 설정
- `ApplicationRunner`로 서버 시작 시 1회 실행

---

## 4. 비즈니스 규칙

| 규칙 | 설명 | HTTP 상태 |
|------|------|-----------|
| 정원 초과 방지 | enrolled >= capacity일 때 거부 | 409 Conflict |
| 중복 수강 방지 | 동일 학생-강좌 조합 거부 | 409 Conflict |
| 학점 제한 | 최대 18학점 초과 시 거부 | 400 Bad Request |
| 시간표 충돌 | 기존 수강 강좌와 시간 겹침 시 거부 | 409 Conflict |

---

## 5. 기술 스택

| 항목 | 선택 | 버전 |
|------|------|------|
| Framework | Spring Boot | 4.0.2 |
| Language | Java | 21 |
| DB | H2 (인메모리) | - |
| ORM | Spring Data JPA + Hibernate | - |
| API 문서 | springdoc-openapi (Swagger UI) | 3.0.1 |
| 빌드 | Gradle | 9.3.0 |
