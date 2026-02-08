# API 문서

> Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 공통 응답 형식

### 성공
```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-02-08T10:30:00"
}
```

### 실패
```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "오류 메시지",
  "timestamp": "2026-02-08T10:30:00"
}
```

## 엔드포인트 목록

### 헬스체크
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/health` | 서버 상태 확인 (데이터 생성 완료 후 200) |

### 강좌
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/courses` | 전체 강좌 목록 (`?department=` 필터 지원) |
| GET | `/courses/{id}` | 강좌 상세 조회 |

### 수강신청
| Method | URL | 설명 |
|--------|-----|------|
| POST | `/enrollments` | 수강신청 |
| DELETE | `/enrollments/{id}` | 수강취소 |
| GET | `/students/{studentId}/enrollments` | 학생 시간표 조회 |

### 학생
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/students` | 학생 목록 (페이징) |
| GET | `/students/{id}` | 학생 상세 조회 |

### 교수
| Method | URL | 설명 |
|--------|-----|------|
| GET | `/professors` | 교수 목록 (페이징) |

---

## API 상세

### GET /health
**성공 (200)**:
```text
OK
```

**초기 데이터 생성 중 (503)**:
```text
초기 데이터 생성 중...
```

---

### GET /courses
**쿼리 파라미터**
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| department | string | 아니오 | 학과명으로 필터 (예: 컴퓨터공학과) |

**성공 (200)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "자료구조",
      "courseCode": "컴퓨001",
      "credits": 3,
      "capacity": 30,
      "enrolled": 25,
      "schedule": "월 09:00-10:30, 수 09:00-10:30",
      "professor": "김민준",
      "department": "컴퓨터공학과"
    }
  ],
  "timestamp": "2026-02-08T10:30:00"
}
```

---

### GET /courses/{id}
**Path 파라미터**
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | number | 예 | 강좌 ID |

**성공 (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "자료구조",
    "courseCode": "컴퓨001",
    "credits": 3,
    "capacity": 30,
    "enrolled": 25,
    "schedule": "월 09:00-10:30, 수 09:00-10:30",
    "professor": "김민준",
    "department": "컴퓨터공학과"
  },
  "timestamp": "2026-02-08T10:30:00"
}
```

**에러**
- 404 `COURSE_NOT_FOUND`

---

### POST /enrollments
**요청**:
```json
{"studentId": 1, "courseId": 1}
```

**성공 (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "studentId": 1,
    "studentName": "홍길동",
    "courseId": 1,
    "courseName": "자료구조",
    "credits": 3,
    "schedule": "월 09:00-10:30, 수 09:00-10:30",
    "enrolledAt": "2026-02-08T10:30:00"
  },
  "timestamp": "2026-02-08T10:30:00"
}
```

**에러**
- 400 `VALIDATION_ERROR`
- 400 `CREDIT_LIMIT_EXCEEDED`
- 404 `STUDENT_NOT_FOUND`
- 404 `COURSE_NOT_FOUND`
- 409 `CAPACITY_EXCEEDED`
- 409 `TIME_CONFLICT`
- 409 `DUPLICATE_ENROLLMENT`

---

### DELETE /enrollments/{id}
**Path 파라미터**
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | number | 예 | 수강신청 ID |

**성공 (200)**:
```json
{
  "success": true,
  "data": null,
  "timestamp": "2026-02-08T10:30:00"
}
```

**에러**
- 404 `ENROLLMENT_NOT_FOUND`
- 404 `COURSE_NOT_FOUND`

---

### GET /students/{studentId}/enrollments
**Path 파라미터**
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| studentId | number | 예 | 학생 ID |

**성공 (200)**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "studentId": 1,
      "studentName": "홍길동",
      "courseId": 1,
      "courseName": "자료구조",
      "credits": 3,
      "schedule": "월 09:00-10:30, 수 09:00-10:30",
      "enrolledAt": "2026-02-08T10:30:00"
    }
  ],
  "timestamp": "2026-02-08T10:30:00"
}
```

**에러**
- 404 `STUDENT_NOT_FOUND`

---

### GET /students
**쿼리 파라미터 (페이징)**
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| page | number | 아니오 | 0 | 페이지 번호 |
| size | number | 아니오 | 20 | 페이지 크기 |
| sort | string | 아니오 | id,asc | 정렬 (예: `id,desc`)<br>방향만 전달한 경우(`sort=DESC/ASC`)에는 `id` 컬럼을 기준으로 정렬 |

- 허용 정렬 컬럼: `id`, `studentNumber`, `name`, `grade`  
- 잘못된 컬럼이 들어오면 `id ASC`로 자동 보정됨.

**요청 예시**
- `/students?page=0&size=20&sort=id,desc`
- `/students?page=0&size=20&sort=grade,ASC`
- `/students?sort=DESC` → `id DESC`로 처리

**성공 (200)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "studentNumber": "202400001",
        "name": "김민준",
        "grade": 2,
        "department": "컴퓨터공학과"
      }
    ],
    "totalElements": 10000,
    "totalPages": 500,
    "size": 20,
    "number": 0
  },
  "timestamp": "2026-02-08T10:30:00"
}
```

---

### GET /students/{id}
**Path 파라미터**
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| id | number | 예 | 학생 ID |

**성공 (200)**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "studentNumber": "202400001",
    "name": "김민준",
    "grade": 2,
    "department": "컴퓨터공학과"
  },
  "timestamp": "2026-02-08T10:30:00"
}
```

**에러**
- 404 `STUDENT_NOT_FOUND`

---

### GET /professors
**쿼리 파라미터 (페이징)**
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| page | number | 아니오 | 0 | 페이지 번호 |
| size | number | 아니오 | 20 | 페이지 크기 |
| sort | string | 아니오 | id,asc | 정렬 (예: `id,desc`)<br>방향만 전달한 경우(`sort=DESC/ASC`)에는 `id` 컬럼을 기준으로 정렬 |

- 허용 정렬 컬럼: `id`, `name`  
- 잘못된 컬럼이 들어오면 `id ASC`로 자동 보정됨.

**요청 예시**
- `/professors?page=0&size=20&sort=name,asc`
- `/professors?page=0&size=20&sort=id,DESC`
- `/professors?sort=DESC` → `id DESC`로 처리

**성공 (200)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "이서윤",
        "department": "전자공학과"
      }
    ],
    "totalElements": 108,
    "totalPages": 6,
    "size": 20,
    "number": 0
  },
  "timestamp": "2026-02-08T10:30:00"
}
```

---

## 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| COURSE_NOT_FOUND | 404 | 강좌 없음 |
| STUDENT_NOT_FOUND | 404 | 학생 없음 |
| ENROLLMENT_NOT_FOUND | 404 | 수강신청 내역 없음 |
| CAPACITY_EXCEEDED | 409 | 정원 초과 |
| CREDIT_LIMIT_EXCEEDED | 400 | 18학점 초과 |
| TIME_CONFLICT | 409 | 시간표 충돌 |
| DUPLICATE_ENROLLMENT | 409 | 중복 수강신청 |
| VALIDATION_ERROR | 400 | 요청 데이터 유효성 실패 |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |
