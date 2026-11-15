# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Personal budget book (가계부) app. The owner is building it partly to learn Kotlin + Spring Boot backend development, so explain non-obvious Kotlin/Spring choices when making changes. Communicate in Korean.

Layout: the repo root holds subprojects. Currently only `backend/` exists (a frontend may be added later as a sibling directory). Git is initialized at the root, not inside `backend/`.

## Backend commands

Run from `backend/` (Gradle wrapper lives there; requires JDK 21):

```bash
./gradlew bootRun                     # run app on http://localhost:8080
./gradlew build                       # compile + test + package
./gradlew test                        # all tests
./gradlew test --tests 'com.budgetbook.SomeTest'              # single test class
./gradlew test --tests 'com.budgetbook.SomeTest.methodName'   # single test method
```

No linter/formatter is configured yet.

`scripts/kill-port.sh [port]` (default 8080) stops whatever is listening on the port — use it to clean up a leftover `bootRun` before starting the app, and after starting the app yourself for manual checks (then confirm the port is free).

## Code layout

Feature packages under `com.budgetbook`: `entry/` (거래 내역 CRUD: entity, repository, service, controller, DTOs), `common/` (`GlobalExceptionHandler`), `hello/` (`GET /hello` health-style endpoint), `local/` (local-only sample data). Layering is Controller → Service (`@Transactional` boundary) → Repository; controllers exchange DTOs, never entities.

Error responses are RFC 9457 `ProblemDetail`. `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler` (which makes Boot's `spring.mvc.problemdetails.enabled` irrelevant) and adds a field-level `errors` array for 400s. Not-found cases throw `ResponseStatusException` subclasses (e.g. `EntryNotFoundException`).

## Profiles and tests

- `spring.profiles.default=local`: running the app without a profile activates `local`, where `SampleDataLoader` (an `ApplicationRunner`) inserts 12 sample entries dated relative to today (this month + last month).
- The Gradle `test` task sets `spring.profiles.active=test`, so tests never see sample data. Tests asserting exact row counts rely on this.
- A test that activates `local` must use its own H2 URL (see `SampleDataLoaderTest`), since `@SpringBootTest` contexts otherwise share `jdbc:h2:mem:budgetbook`.
- Test slices in use: `@DataJpaTest` (repository), `@WebMvcTest` + `@MockitoBean` (web layer only), `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` (full-stack API tests). Boot 4 packages: `org.springframework.boot.webmvc.test.autoconfigure.*`, `org.springframework.boot.data.jpa.test.autoconfigure.*`.

## Backend stack notes

- Spring Boot 4.x / Kotlin 2.3 / Java 21 toolchain, Gradle Kotlin DSL. Root package `com.budgetbook`.
- Boot 4 uses modular starters: web is `spring-boot-starter-webmvc` (not `-web`), H2 console is a separate `spring-boot-h2console` module, and test slices have their own `*-test` starters (e.g. `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`).
- Jackson 3 is used (`tools.jackson.*` packages, not `com.fasterxml.jackson.*`).
- `kotlin("plugin.jpa")` + `allOpen` for `@Entity`/`@MappedSuperclass`/`@Embeddable` are configured, so JPA entities can be plain Kotlin classes without manual `open`.
- Compiler flag `-Xjsr305=strict` makes Spring's nullability annotations map to Kotlin nullable types.

## Database (dev)

H2 in-memory (`jdbc:h2:mem:budgetbook`, user `sa`, empty password) with `ddl-auto=create-drop` — schema is generated from entities and all data is lost on restart (the `local` profile re-seeds sample data on each start). H2 console: http://localhost:8080/h2-console. SQL logging is enabled (`show-sql`, `format_sql`). No migration tool (Flyway etc.) yet.
