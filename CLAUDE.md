# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Personal budget book (가계부) app. The owner is building it partly to learn Kotlin + Spring Boot backend development, so explain non-obvious Kotlin/Spring choices when making changes. Communicate in Korean.

Layout: the repo root holds subprojects — `backend/` (Spring Boot API) and `frontend/` (React + Vite UI) — plus `docs/`. Git is initialized at the root, not inside the subprojects.

## Frontend

React 19 + TypeScript + Vite, scaffolded with `create-vite` (react-ts template; lint is oxlint, not ESLint). The owner is a backend developer learning from reading this code, so keep it plain and comment non-obvious Tailwind/React bits. Styling is Tailwind CSS v4 + shadcn/ui; no other libraries (state management, router, etc.) unless asked.

- Style with Tailwind classes in `className`; there are no per-component CSS files. `src/index.css` is exactly what `shadcn init` generated (Tailwind/shadcn imports + theme variables like `--background`, `--muted-foreground`) — keep it pristine; don't hand-edit or reformat it. Dark mode uses shadcn's default `.dark` class on `<html>`; an inline script in `index.html` `<head>` toggles that class from the OS `prefers-color-scheme` setting (in `<head>` so it runs before first paint).
- `src/components/ui/` is shadcn-generated code (style `radix-nova`, see `components.json`). Add components with `npx shadcn@4.23.0 add <name>` rather than writing them by hand; prefer using them as-is over editing them. `cn()` (`src/lib/utils.ts`, re-exported from shadcn's `cn` package) merges class names. `src/hooks/` holds shadcn-generated hooks too (`use-mobile.ts`, added with `sidebar`). `.oxlintrc.json` overrides turn off only the rules generated code trips: `react/only-export-components` in `src/components/ui/**`, `react/set-state-in-effect` in `src/hooks/**`.
- Import alias `@/` → `src/` is configured in both `vite.config.ts` (`resolve.alias`) and `tsconfig.json`/`tsconfig.app.json` (`paths`, no `baseUrl` on TS 7).
- Currently one page: `App.tsx` renders `MonthlyEntries.tsx` (월별 거래 내역: ‹ › month navigation, refetched in `useEffect` keyed on `month`, 수입/지출 split client-side by `type` into two `Card`+`Table`s laid out with `grid md:grid-cols-2`). `GET /api/entries` takes `?month=YYYY-MM` (`YearMonth`) and defaults to the current month — there is no all-entries endpoint. API types and fetch functions live in `src/api.ts`, mirroring the backend DTOs.

Run from `frontend/`:

```bash
npm run dev      # dev server on http://localhost:5173
npm run build    # tsc type check + production build
npm run lint     # oxlint
```

The Vite dev server proxies `/api` to `http://localhost:8080` (`vite.config.ts`), so UI code calls relative `/api/...` URLs and the backend needs no CORS config. Clean up a dev server you started with `backend/scripts/kill-port.sh 5173`.

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

Requirements, their status, and the decision log live in `docs/requirements.md` — read it before adding features, and update statuses/decisions there when a change lands.

Feature packages under `com.budgetbook`: `entry/` (거래 내역 CRUD: entity, repository, service, controller, DTOs), `category/` (카테고리 CRUD + `DefaultCategoryInitializer`, which seeds default categories in every profile), `common/` (`GlobalExceptionHandler`), `hello/` (`GET /hello` health-style endpoint), `local/` (local-only sample data). `Entry` references `Category` via a LAZY `@ManyToOne`; list queries use `@EntityGraph` to avoid N+1. Startup runners are ordered with `@Order` (default categories `1` before sample data `2`). Layering is Controller → Service (`@Transactional` boundary) → Repository; controllers exchange DTOs, never entities.

Error responses are RFC 9457 `ProblemDetail`. `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler` (which makes Boot's `spring.mvc.problemdetails.enabled` irrelevant) and adds a field-level `errors` array for 400s. Not-found cases throw `ResponseStatusException` subclasses (e.g. `EntryNotFoundException`).

## Profiles and tests

- `spring.profiles.default=local`: running the app without a profile activates `local`, where `SampleDataLoader` (an `ApplicationRunner`) inserts 12 sample entries dated relative to today (this month + last month).
- Tests must never see sample data; tests asserting exact row counts rely on this. Every `@SpringBootTest` class carries `@ActiveProfiles("test")` so this holds in any runner — the Gradle `test` task also sets `spring.profiles.active=test`, but IDE test runners (VS Code Java Test Runner) bypass Gradle and would otherwise fall back to `local`. Add the annotation to new `@SpringBootTest` classes. Slice tests (`@WebMvcTest`, `@DataJpaTest`) don't run `ApplicationRunner`s, so they don't need it. Default categories *are* present in `@SpringBootTest` tests (look them up via `CategoryRepository.findByTypeAndName`), but not in `@DataJpaTest`, which doesn't run `ApplicationRunner`s.
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
