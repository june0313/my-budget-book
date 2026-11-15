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

### 테스트

```bash
cd backend
./gradlew test                                                # 전체 테스트
./gradlew test --tests 'com.budgetbook.SomeTest'              # 특정 테스트 클래스
./gradlew test --tests 'com.budgetbook.SomeTest.methodName'   # 특정 테스트 메서드
```

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
