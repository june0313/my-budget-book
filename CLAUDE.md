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

## Backend stack notes

- Spring Boot 4.x / Kotlin 2.3 / Java 21 toolchain, Gradle Kotlin DSL. Root package `com.budgetbook`.
- Boot 4 uses modular starters: web is `spring-boot-starter-webmvc` (not `-web`), H2 console is a separate `spring-boot-h2console` module, and test slices have their own `*-test` starters (e.g. `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`).
- Jackson 3 is used (`tools.jackson.*` packages, not `com.fasterxml.jackson.*`).
- `kotlin("plugin.jpa")` + `allOpen` for `@Entity`/`@MappedSuperclass`/`@Embeddable` are configured, so JPA entities can be plain Kotlin classes without manual `open`.
- Compiler flag `-Xjsr305=strict` makes Spring's nullability annotations map to Kotlin nullable types.

## Database (dev)

H2 in-memory (`jdbc:h2:mem:budgetbook`, user `sa`, empty password) with `ddl-auto=create-drop` — schema is generated from entities and all data is lost on restart. H2 console: http://localhost:8080/h2-console. SQL logging is enabled (`show-sql`, `format_sql`). No migration tool (Flyway etc.) yet.
