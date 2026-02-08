# 수강신청 시스템 - AI 코딩 지침

> 이 문서는 AI 에이전트가 코드를 작성할 때 반드시 따라야 할 규칙입니다.

---

## 🎯 절대 원칙 (최우선)

### 1. 동시성 제어는 타협 불가
- **수강신청/취소 로직에는 반드시 동시성 제어를 적용**
- 정원 초과는 0.01%도 허용 안 됨
- 락 메커니즘 선택 시 반드시 근거를 docs/REQUIREMENTS.md에 기록
- 동시성 제어 코드 작성 후 "정원 1명, 동시 요청 100명" 시나리오로 검증 요청

### 2. 데이터 생성 규칙
```
✅ 허용:
- 코드에 소규모 토큰 배열 정의
  const names = ['김민준', '이서윤', '박지호', ...]
  const departments = ['컴퓨터공학과', '전자공학과', ...]

❌ 금지:
- SQL 파일, CSV, JSON 파일 사용
- 완성된 레코드를 코드에 직접 작성
  const students = [
    {id: 1, name: "김민준", ...},  // 이런 거 안 됨
    {id: 2, name: "이서윤", ...},
  ]
```

- **반드시 런타임에 반복문으로 생성**
- 1분 이내 완료되도록 배치 삽입 사용
- 생성 로직은 별도 서비스/유틸 클래스로 분리

### 3. API 응답 형식 통일
```json
// 성공
{
  "success": true,
  "data": { ... }
}

// 실패
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "사용자에게 보여줄 메시지",
  "timestamp": "2025-02-08T10:30:00Z"
}
```

---

## 📁 파일 구조 및 명명 규칙

### Spring Boot
```
src/main/java/com/example/enrollment/
├── domain/
│   ├── Student.java
│   ├── Course.java
│   ├── Enrollment.java
│   └── Professor.java
├── repository/
│   ├── StudentRepository.java
│   └── CourseRepository.java
├── service/
│   ├── EnrollmentService.java      # 동시성 제어 여기서
│   ├── CourseService.java
│   └── DataGenerationService.java  # 초기 데이터 생성
├── controller/
│   ├── EnrollmentController.java
│   ├── CourseController.java
│   └── HealthController.java       # /health 엔드포인트
└── dto/
    ├── EnrollmentRequest.java
    └── CourseResponse.java
```

### Python (FastAPI)
```
src/
├── models/
│   ├── student.py
│   ├── course.py
│   └── enrollment.py
├── repositories/
│   ├── student_repository.py
│   └── course_repository.py
├── services/
│   ├── enrollment_service.py       # 동시성 제어 여기서
│   └── data_generator.py           # 초기 데이터 생성
├── routers/
│   ├── enrollment_router.py
│   ├── course_router.py
│   └── health_router.py
└── schemas/
    └── enrollment_schema.py
```

---

## 🔒 동시성 제어 구현 가이드

### 비관적 락 예시 (Spring Boot + JPA)
```java
@Transactional
public EnrollmentResult enroll(Long studentId, Long courseId) {
    // 1. 강좌를 락과 함께 조회
    Course course = courseRepository.findByIdWithLock(courseId)
        .orElseThrow(() -> new CourseNotFoundException());
    
    // 2. 정원 체크
    if (course.getEnrolled() >= course.getCapacity()) {
        throw new CapacityExceededException();
    }
    
    // 3. 학생 조회 및 학점/시간 검증
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new StudentNotFoundException());
    
    validateCreditLimit(student, course);
    validateTimeConflict(student, course);
    
    // 4. 수강신청 처리
    Enrollment enrollment = new Enrollment(student, course);
    course.incrementEnrolled();
    
    enrollmentRepository.save(enrollment);
    courseRepository.save(course);
    
    return EnrollmentResult.success(enrollment);
}
```

### Repository에 락 쿼리 메서드
```java
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findByIdWithLock(@Param("id") Long id);
}
```

---

## 📊 데이터 생성 가이드

### 기본 원칙
- 최소 규모: 학과 10+, 강좌 500+, 학생 10,000+, 교수 100+
- 토큰 배열 → 반복문으로 조합 → 배치 삽입
- 1분 이내 완료 목표

### 구현 패턴
```java
@Component
public class DataGenerationService {
    
    private static final String[] LAST_NAMES = {"김", "이", "박", "최", "정"};
    private static final String[] FIRST_NAMES = {"민준", "서윤", "지호", "서연", "도윤"};
    
    private static final String[] DEPARTMENTS = {
        "컴퓨터공학과", "전자공학과", "기계공학과", 
        "경영학과", "경제학과", "수학과"
    };
    
    private static final String[] COURSE_PREFIXES = {
        "자료구조", "알고리즘", "데이터베이스", 
        "운영체제", "컴퓨터구조", "네트워크"
    };
    
    @PostConstruct
    public void generateInitialData() {
        long startTime = System.currentTimeMillis();
        
        List<Department> departments = generateDepartments();
        departmentRepository.saveAll(departments);  // 배치 삽입
        
        List<Professor> professors = generateProfessors(departments);
        professorRepository.saveAll(professors);
        
        List<Course> courses = generateCourses(departments, professors);
        courseRepository.saveAll(courses);
        
        List<Student> students = generateStudents(departments);
        studentRepository.saveAll(students);
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Data generation completed in {}ms", elapsed);
    }
    
    private List<Student> generateStudents(List<Department> departments) {
        List<Student> students = new ArrayList<>(10000);
        Random random = new Random();
        
        for (int i = 0; i < 10000; i++) {
            String name = LAST_NAMES[random.nextInt(LAST_NAMES.length)] 
                        + FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String studentId = String.format("2024%05d", i + 1);
            Department dept = departments.get(random.nextInt(departments.size()));
            
            students.add(new Student(studentId, name, dept));
        }
        
        return students;
    }
    
    // generateCourses, generateProfessors 등도 유사한 패턴
}
```

---

## 🔍 비즈니스 규칙 검증

### 학점 제한 검증
```java
private void validateCreditLimit(Student student, Course course) {
    int currentCredits = enrollmentRepository
        .sumCreditsByStudentId(student.getId());
    
    if (currentCredits + course.getCredits() > 18) {
        throw new CreditLimitExceededException(
            "최대 18학점까지만 신청 가능합니다. " +
            "현재: " + currentCredits + "학점"
        );
    }
}
```

### 시간 충돌 검증
```java
private void validateTimeConflict(Student student, Course newCourse) {
    List<Enrollment> enrollments = enrollmentRepository
        .findByStudentId(student.getId());
    
    for (Enrollment enrollment : enrollments) {
        Course existingCourse = enrollment.getCourse();
        if (hasTimeConflict(existingCourse, newCourse)) {
            throw new TimeConflictException(
                existingCourse.getName() + "과 시간이 겹칩니다"
            );
        }
    }
}

private boolean hasTimeConflict(Course c1, Course c2) {
    // 시간표 문자열 파싱 후 비교
    // "월 09:00-10:30" 형식 가정
    Schedule s1 = Schedule.parse(c1.getSchedule());
    Schedule s2 = Schedule.parse(c2.getSchedule());
    
    return s1.getDayOfWeek() == s2.getDayOfWeek() &&
           s1.overlaps(s2);
}
```

---

## 📝 필수 구현 API

### 1. 헬스체크 (최우선)
```java
@GetMapping("/health")
public ResponseEntity<String> health() {
    return ResponseEntity.ok("OK");
}
```
- 데이터 생성 완료 후 응답해야 함
- 이 엔드포인트가 200 응답하면 모든 API 사용 가능한 상태여야 함

### 2. 강좌 목록 조회
```java
@GetMapping("/courses")
public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourses(
    @RequestParam(required = false) String department
) {
    List<CourseResponse> courses = department != null
        ? courseService.getCoursesByDepartment(department)
        : courseService.getAllCourses();
    
    return ResponseEntity.ok(ApiResponse.success(courses));
}
```

**필수 응답 필드**:
```json
{
  "id": 1,
  "name": "자료구조",
  "credits": 3,
  "capacity": 30,
  "enrolled": 25,
  "schedule": "월 09:00-10:30",
  "professor": "김교수",
  "department": "컴퓨터공학과"
}
```

### 3. 수강신청
```java
@PostMapping("/enrollments")
public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
    @RequestBody EnrollmentRequest request
) {
    try {
        EnrollmentResponse response = enrollmentService.enroll(
            request.getStudentId(), 
            request.getCourseId()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
        
    } catch (CapacityExceededException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("CAPACITY_EXCEEDED", e.getMessage()));
            
    } catch (CreditLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("CREDIT_LIMIT_EXCEEDED", e.getMessage()));
            
    } catch (TimeConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("TIME_CONFLICT", e.getMessage()));
    }
}
```

---

## ⚠️ 에러 처리 규칙

### HTTP 상태 코드 사용
- `200 OK`: 성공
- `400 Bad Request`: 잘못된 요청 (필수 파라미터 누락 등)
- `404 Not Found`: 리소스 없음 (학생/강좌 ID 없음)
- `409 Conflict`: 비즈니스 규칙 위반 (정원 초과, 시간 충돌)
- `500 Internal Server Error`: 서버 내부 오류

### 에러 메시지는 명확하게
```
❌ "처리 실패"
❌ "에러 발생"

✅ "강좌 정원이 초과되었습니다 (정원: 30명, 신청: 30명)"
✅ "최대 18학점까지만 신청 가능합니다 (현재: 15학점, 신청: 4학점)"
✅ "'자료구조'와 시간이 겹칩니다 (월 09:00-10:30)"
```

---

## 🧪 테스트 가이드

### 동시성 테스트 (필수)
```java
@Test
void 동시에_100명이_정원_1명_강좌에_신청하면_1명만_성공() throws Exception {
    // Given: 정원 1명, 신청 0명인 강좌
    Course course = courseRepository.save(
        Course.builder()
            .name("테스트강좌")
            .capacity(1)
            .enrolled(0)
            .build()
    );
    
    // When: 100명이 동시 신청
    ExecutorService executor = Executors.newFixedThreadPool(100);
    CountDownLatch latch = new CountDownLatch(100);
    AtomicInteger successCount = new AtomicInteger(0);
    
    for (int i = 0; i < 100; i++) {
        final int studentIndex = i;
        executor.submit(() -> {
            try {
                enrollmentService.enroll(studentIndex + 1L, course.getId());
                successCount.incrementAndGet();
            } catch (Exception e) {
                // 예상된 실패
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    
    // Then: 정확히 1명만 성공
    assertThat(successCount.get()).isEqualTo(1);
    
    Course updatedCourse = courseRepository.findById(course.getId()).get();
    assertThat(updatedCourse.getEnrolled()).isEqualTo(1);
}
```

---

## 📚 문서화 규칙

### docs/REQUIREMENTS.md에 반드시 포함
1. **동시성 제어 전략**
    - 선택한 방법 (비관적 락/낙관적 락/분산 락 등)
    - 선택 근거
    - 트레이드오프 분석

2. **시간표 구조 정의**
    - 요일/시간 표현 방식
    - 충돌 판단 로직

3. **데이터 생성 전략**
    - 현실적인 데이터 생성 방법
    - 성능 최적화 방법

### docs/API.md에 반드시 포함
- 모든 엔드포인트 URL, 메서드
- 요청/응답 예제
- 에러 코드별 응답

---

## ✅ 구현 체크리스트

### Phase 1: 빌드 및 실행 가능한 상태 (최우선)
- [ ] 프로젝트 생성 및 빌드 설정
- [ ] `/health` 엔드포인트 구현
- [ ] 기본 Entity 정의
- [ ] 데이터 생성 로직 (배치 삽입, 1분 이내)
- [ ] 강좌 목록 조회 API

### Phase 2: 핵심 기능 (동시성 제어)
- [ ] 수강신청 API + 동시성 제어
- [ ] 정원 초과 방지 검증
- [ ] 학점 제한 검증 (18학점)
- [ ] 시간 충돌 검증
- [ ] 수강취소 API
- [ ] 내 시간표 조회 API

### Phase 3: 안정성
- [ ] 통합된 에러 응답 형식
- [ ] 로깅 추가
- [ ] 동시성 테스트 작성
- [ ] API 문서 완성

---

## 🚫 하지 말아야 할 것

### 절대 금지
1. **정적 데이터 파일 사용** (SQL, CSV, JSON)
2. **완성된 레코드를 코드에 직접 작성**
3. **동시성 제어 없이 수강신청 구현**
4. **의미 없는 데이터** ("Student1", "Course001")

### 지양해야 할 것
1. 너무 복잡한 인증/인가 로직 (시간 부족 시 생략 가능)
2. 불필요한 추가 기능 (선수과목, 학년 제한 등)
3. 과도한 최적화 (기본 동작 우선)

---

## 💡 코드 작성 시 자주 확인할 것

1. **동시성 제어가 적용되었는가?**
    - 수강신청/취소 메서드에 @Transactional + 락

2. **배치 삽입을 사용했는가?**
    - saveAll() 사용, 반복문에서 save() 호출 금지

3. **에러 메시지가 명확한가?**
    - 사용자가 왜 실패했는지 알 수 있는가?

4. **API 응답 형식이 통일되었는가?**
    - 모든 API가 동일한 구조 사용

5. **문서화가 되어있는가?**
    - 설계 결정은 REQUIREMENTS.md에
    - API 명세는 API.md에

---

**핵심 목표**: 정원 1명, 동시 요청 100명 → 정확히 1명만 성공 ✅