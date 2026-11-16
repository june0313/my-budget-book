# my-budget-book

개인 가계부 애플리케이션입니다. 백엔드는 Kotlin + Spring Boot로 만들고 있습니다.

## 프로젝트 구조

```
my-budget-book/
└── backend/   # Kotlin + Spring Boot API 서버
```

## 백엔드

### 기술 스택

- Kotlin 2.3 / Java 21
- Spring Boot 4.1 (Web MVC, Data JPA, Validation)
- H2 Database (개발용 인메모리 DB)
- Gradle (Kotlin DSL)

### 준비물

- **JDK 21**

  아래 명령으로 설치된 버전을 확인할 수 있습니다.

  ```bash
  java -version
  ```

  Gradle은 따로 설치하지 않아도 됩니다. 프로젝트에 포함된 Gradle Wrapper(`./gradlew`)를 사용합니다.

### 실행

```bash
cd backend
./gradlew bootRun
```

서버가 뜨면 http://localhost:8080 으로 접속할 수 있습니다. 처음 실행할 때는 의존성을 내려받느라 시간이 조금 걸립니다.

이전에 띄운 서버가 8080 포트를 계속 점유하고 있으면 아래 스크립트로 정리할 수 있습니다.

```bash
cd backend
scripts/kill-port.sh          # 8080 포트를 점유한 프로세스 종료
scripts/kill-port.sh 3000     # 다른 포트 지정
```

정상 종료(SIGTERM)를 먼저 시도하고, 10초 안에 종료되지 않으면 강제 종료(SIGKILL)합니다.

### API

| 메서드 | 경로 | 설명 |
|---|---|---|
| `GET` | `/hello` | 서버 상태 확인 (인사말, 현재 시각, Java 버전) |
| `GET` | `/api/entries` | 거래 내역 목록 (최신 날짜순) |
| `GET` | `/api/entries/{id}` | 거래 내역 단건 조회 |
| `POST` | `/api/entries` | 거래 내역 등록 (201 + `Location` 헤더) |
| `PUT` | `/api/entries/{id}` | 거래 내역 수정 (전체 교체) |
| `DELETE` | `/api/entries/{id}` | 거래 내역 삭제 (204) |
| `GET` | `/api/categories` | 카테고리 목록 (`?type=EXPENSE`처럼 구분으로 거르기 가능) |
| `POST` | `/api/categories` | 카테고리 추가 (201) |
| `PATCH` | `/api/categories/{id}` | 카테고리 이름 변경 |
| `DELETE` | `/api/categories/{id}` | 카테고리 삭제 (204). 거래 내역이 사용 중이면 409 |

거래 내역 등록/수정 요청 본문 예시:

```json
{
  "date": "2026-11-15",
  "type": "EXPENSE",
  "amount": 12000,
  "categoryId": 1,
  "memo": "점심"
}
```

- `type`: `INCOME`(수입) 또는 `EXPENSE`(지출)
- `amount`: 0보다 큰 정수(원)
- `categoryId`: `GET /api/categories`에서 고른 카테고리의 id. 거래의 `type`과 카테고리의 구분이 같아야 합니다
- `memo`: 선택

거래 내역 응답에는 카테고리 이름이 함께 담깁니다.

```json
{ "id": 1, "date": "2026-11-15", "type": "EXPENSE", "amount": 12000, "category": { "id": 1, "name": "식비" }, "memo": "점심" }
```

카테고리 추가 / 이름 변경 요청 본문 예시:

```json
{ "name": "반려동물", "type": "EXPENSE" }
```

```json
{ "name": "외식" }
```

앱이 시작될 때 기본 카테고리(지출 9개, 수입 4개)가 자동으로 등록됩니다. 자세한 규칙은 [요구사항 문서](docs/requirements.md)를 참고하세요.

에러 응답은 [RFC 9457 ProblemDetail](https://www.rfc-editor.org/rfc/rfc9457) 형식입니다. 입력값이 잘못된 경우(400)에는 `errors` 배열에 필드별 원인이 담깁니다.

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "입력값이 올바르지 않습니다",
  "instance": "/api/entries",
  "errors": [
    { "field": "amount", "message": "금액은 0보다 커야 합니다" }
  ]
}
```

### 테스트

```bash
cd backend
./gradlew test                                                # 전체 테스트
./gradlew test --tests 'com.budgetbook.SomeTest'              # 특정 테스트 클래스
./gradlew test --tests 'com.budgetbook.SomeTest.methodName'   # 특정 테스트 메서드
```

테스트는 `test` 프로필로 실행되므로 로컬 샘플 데이터가 들어가지 않습니다(`build.gradle.kts`에서 지정). IntelliJ에서 테스트를 실행할 때는 **Settings → Build, Execution, Deployment → Build Tools → Gradle → Run tests using**이 `Gradle`로 되어 있어야 이 설정이 적용됩니다.

### 빌드

```bash
cd backend
./gradlew build
```

빌드 결과물(jar)은 `backend/build/libs/`에 생성됩니다.

### 데이터베이스 (H2)

개발 환경에서는 H2 인메모리 DB를 사용합니다. **앱을 재시작하면 데이터가 모두 초기화됩니다.** 테이블은 엔티티 클래스를 기준으로 자동 생성됩니다.

앱이 실행 중일 때 브라우저에서 H2 콘솔로 DB를 직접 확인할 수 있습니다.

- 주소: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:budgetbook`
- 사용자명: `sa`
- 비밀번호: (비워둠)

실행되는 SQL은 애플리케이션 콘솔 로그에 출력됩니다.

### 프로필과 샘플 데이터

| 프로필 | 언제 | 동작 |
|---|---|---|
| `local` | 프로필을 따로 지정하지 않고 실행할 때 (기본값) | 앱이 뜰 때 샘플 거래 내역 12건을 자동 등록 |
| `test` | `./gradlew test` 실행 시 | 샘플 데이터 없음 |

샘플 데이터는 실행한 날 기준 **이번 달 6건, 지난달 6건**으로 만들어집니다. DB에 이미 데이터가 있으면 등록하지 않습니다. 내용은 `backend/src/main/kotlin/com/budgetbook/local/SampleDataLoader.kt`에서 바꿀 수 있습니다.

샘플 데이터 없이 실행하려면 다른 프로필을 지정합니다.

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```
