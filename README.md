# 대학교 수강신청 시스템

## 요구사항
- Java 21
- Gradle (Wrapper 포함)

## 빌드
```bash
./gradlew clean build
```

## 실행
```bash
./gradlew bootRun
```

## 테스트
```bash
./gradlew test
```

## 접속 정보
- 서버: `http://localhost:8080`
- 헬스체크: `GET /health`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 콘솔: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:coursedb`
  - 사용자명: `sa`
  - 비밀번호: (없음)

## 데이터 초기화
- 서버 시작 시 초기 데이터가 생성됩니다.
- 초기 데이터 생성 중에는 `GET /health`가 503을 반환하며, 완료 후 200 OK를 반환합니다.

