# Grids & Circle — 카페 메뉴 관리 서비스 (NBE13-15-1-team01)

로컬 카페 Grids & Circle의 원두 패키지 주문·상점 관리 백엔드입니다. Spring Boot로 카테고리·메뉴 CRUD, 주문·배송 묶음·통계 API 24개(A01~A24)를 구현하고 제공된 웹 화면에 연결합니다.

> 참고 산출물: [API 명세서](https://app.notion.com/p/API-40c15a01205480e6bb31c07909d68298?source=copy_link) · [기획서](https://app.notion.com/p/40c15a0120548005b624d00c8347f2b2?source=copy_link) · [ERD](https://app.notion.com/p/ERD-40d15a01205480f5bbf1c2358e38851d?source=copy_link)

---

## 1. 팀원별 담당 작업

| 팀원 | 담당 API | 담당 도메인·공통 작업 |
| --- | --- | --- |
| 팀원1 (이름) | A01~A10 카테고리·메뉴 CRUD | `category`, `menu` 패키지, 프로젝트 초기 설정, 초기 데이터, CORS |
| 팀원2 (이름) | A11, A12, A13, A16, A22, A23 주문 | `order` 패키지, 배송일·취소 마감 계산(`Clock`), 트랜잭션 |
| 팀원3 (이름) | A14, A17, A18, A24 배송 묶음·통계 | `delivery`, `statistics` 패키지, 집계 쿼리, 통합 테스트 주도 |
| 팀원4 (이름) | A15, A19 | 메뉴 조회 확장, PPT 제작 |
| 팀원5 (이름) | A20, A21 | 메뉴 검색·판매 여부, 발표·시연, 웹 화면 연결 |

### 공동 구현 작업의 분담

| 작업 | 팀원 | 각자 한 일 |
| --- | --- | --- |
| DB 설계·ERD | 팀원1, 팀원2 | 팀원1: category·menu 테이블 / 팀원2: orders·order_item 테이블 |
| 공통 예외 처리 | 팀원1, 팀원2 | 팀원1: `GlobalExceptionHandler`, `ErrorResponse` / 팀원2: 커스텀 예외 클래스, 400→404→409 순서 |
| CORS·웹 화면 연결 | 팀원1, 팀원5 | 팀원1: `WebConfig` / 팀원5: 화면 탭별 동작 확인 |
| `requests/api-check.http` | 전원 | 각자 담당 API의 정상 요청 + 오류 요청(아래 6절 참고) |
| Controller 테스트 6개 | 전원 | 각자 1개 이상(아래 6절 표 참고) |

---

## 2. 구현 현황

### 완료 기능

| 번호 | 기능 | 상태 |
| --- | --- | --- |
| A01~A05 | 카테고리 CRUD | ✅ 완료 |
| A06~A10 | 메뉴 CRUD | ✅ 완료 |
| A11~A13 | 주문 접수·목록·상세 | ✅ 완료 |
| A14 | 배송 묶음 | ⬜ 미착수 |
| A15, A19, A20, A21 | 메뉴 조회·검색·판매 여부 | ⬜ 미착수 |
| A16, A22, A23 | 주문 취소·메모·재주문 | ⬜ 미착수 |
| A17, A18, A24 | 접수일별 통계 | ⬜ 미착수 |

### 미완료 기능

*{예시}*
- A09 메뉴 일반 수정 — Controller·DTO 완료, Service에서 기존 available 유지 처리 남음
- A14, A15~A24 — 미착수

### 미해결 오류

- 없음 *(있으면 "현상 / 재현 방법 / 원인 추정" 형식으로 적기)*

---

## 3. 개발 환경

| 항목 | 버전 |
| --- | --- |
| Java | 21.0.12 |
| Spring Boot | 4.1.2 |
| Gradle | 9.8.2 |
| MySQL | 8.4.11 |
| IDE | IntelliJ IDEA Ultimate |

### DB 설정 위치와 환경변수

설정 파일: `src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:cafe_db}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none              # 테이블은 schema.sql로 관리
    defer-datasource-initialization: true
  sql:
    init:
      mode: always                # 매 실행마다 schema.sql → data.sql 실행
  jackson:
    time-zone: Asia/Seoul
```

| 환경변수 | 의미 | 기본값 | 예시 값 |
| --- | --- | --- | --- |
| `DB_HOST` | MySQL 호스트 | `localhost` | `localhost` |
| `DB_PORT` | MySQL 포트 | `3306` | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `cafe_db` | `cafe_db` |
| `DB_USERNAME` | DB 사용자 | `root` | `root` |
| `DB_PASSWORD` | DB 비밀번호 | 없음(필수) | `your_password` |

`DB_PASSWORD`만 필수이며 나머지는 기본값을 씁니다. IntelliJ에서는 Run → Edit Configurations → Environment variables에 `DB_PASSWORD=...`를 넣고, 터미널에서는 `DB_PASSWORD=... ./gradlew bootRun`으로 실행합니다. 실제 비밀번호는 저장소에 올리지 않습니다.

---

## 4. 새 DB 준비·초기 데이터 등록

애플리케이션을 실행하면 `src/main/resources/schema.sql`과 `data.sql`이 자동 실행되어 테이블과 초기 데이터가 준비됩니다. 사람이 할 일은 빈 데이터베이스를 만드는 것뿐입니다.

1. MySQL 접속 후 데이터베이스 생성
   ```sql
   CREATE DATABASE cafe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;
   ```
2. 3절의 환경변수(`DB_PASSWORD` 등)를 설정합니다.
3. 백엔드를 실행합니다(5절). 기동 시 다음이 순서대로 실행됩니다.
  - `schema.sql` — `category`, `menu`, `orders`, `order_item` 테이블 생성 (`CREATE TABLE IF NOT EXISTS`)
  - `data.sql` — 카테고리 2개(싱글오리진, 블렌드), 메뉴 4개 등록 (`INSERT IGNORE`)
4. 확인
   ```sql
   SELECT * FROM category;   -- 2행
   SELECT * FROM menu;       -- 4행, 모두 category_id=싱글오리진, available=1
   ```

`spring.sql.init.mode=always`라 앱을 다시 실행해도 두 스크립트가 매번 돌지만, `IF NOT EXISTS`·`INSERT IGNORE`로 작성되어 있어 중복 생성·중복 등록이 일어나지 않습니다. DB를 완전히 초기화하려면 `DROP DATABASE cafe_db;` 후 1번부터 다시 진행합니다.

---

## 5. 실행 방법

### 백엔드

```bash
# 프로젝트 루트에서
./gradlew bootRun
```

- 주소: `http://127.0.0.1:8080`
- API 경로: `http://127.0.0.1:8080/api/v1/...`

### 제공 웹 화면

```bash
cd web
java -m jdk.httpserver -b 127.0.0.1 -p 8000
```

- 주소: `http://127.0.0.1:8000/`
- 접속 후 **연결 설정 → 실제 API 연결** 선택, API 서버 주소에 `http://127.0.0.1:8080` 입력 (끝에 `/api/v1`을 붙이지 않음)
- `localhost`가 아닌 `127.0.0.1`로 통일합니다 (CORS 허용 Origin이 `http://127.0.0.1:8000`).

---

## 6. 테스트

### 자동 테스트 실행

```bash
./gradlew test
```

결과 리포트: `build/reports/tests/test/index.html`

### 기본 Controller 테스트 6개

| 분류 | 테스트 클래스·메서드 | 확인하는 요구사항 | 작성자 |
| --- | --- | --- | --- |
| 정상 1 | `CategoryControllerTest.list_returns200` | A01이 200과 id·name 배열을 반환한다 | 팀원1 |
| 정상 2 | `MenuControllerTest.delete_returns204` | A10이 204와 빈 본문을 반환한다 | 팀원4 |
| 입력 오류 1 | `OrderControllerTest.create_invalidPostcode_returns400` | 우편번호 5자리 미달 시 400·VALIDATION_ERROR·fieldErrors.postcode | 팀원2 |
| 입력 오류 2 | `MenuControllerTest.availability_null_returns400` | available null/문자열 시 400·VALIDATION_ERROR | 팀원5 |
| 대상 없음 | `OrderControllerTest.get_notFound_returns404` | 없는 주문 조회 시 404·ORDER_NOT_FOUND·fieldErrors={} | 팀원2 |
| 업무 위반 | `OrderControllerTest.cancel_afterDeadline_returns409` | 마감 후 취소 시 409·ORDER_NOT_CANCELLABLE | 팀원3 |

### `requests/api-check.http`

- 경로: `requests/api-check.http`
- 변수(파일 상단):
  ```
  @baseUrl = http://127.0.0.1:8080/api/v1
  @categoryId = 1
  @menuId = 1
  @orderId = 1
  @email = buyer@example.com
  @date = 2026-11-03
  ```
- 실행 순서: 초기 데이터 등록 상태에서 **위에서 아래로** 실행합니다. A03(카테고리 등록)·A08(메뉴 등록)·A11(주문 접수) 응답의 `id`를 각각 `@categoryId`·`@menuId`·`@orderId`에 수동으로 넣은 뒤 이후 요청을 실행합니다.
- 구성: 정상 24개(A01~A24) + 오류 6개
  - 400 ×2: A11 잘못된 이메일 형식 / A08 가격 범위 초과
  - 404 ×1: A07 없는 menuId
  - 409 ×3: A03 카테고리명 중복 / A11 판매 중지 메뉴 포함 / A11 배송지 불일치
- 각 요청 위 주석에 준비 데이터·기대 상태 코드·비교할 응답 값을 적었습니다.

### 시각 경계값(13:59:59 / 14:00:00) 확인 방법

PC·서버 시계를 바꾸지 않고 `Clock` 빈을 주입해 확인합니다.

- 운영 코드: `ClockConfig`에서 `Clock.system(ZoneId.of("Asia/Seoul"))`을 빈으로 등록하고 `OrderService`가 주입받아 접수 시각·배송일·취소 마감을 계산합니다.
- 테스트: `OrderServiceTest`에서 `Clock.fixed(...)`로 시각을 고정합니다.
  - `2026-11-03T13:59:59+09:00` 접수 → 배송일 `2026-11-03`
  - `2026-11-03T14:00:00+09:00` 접수 → 배송일 `2026-11-04`
  - 배송일 `2026-11-03` 주문을 `13:59:59`에 취소 → 200, `14:00:00`에 취소 → 409 ORDER_NOT_CANCELLABLE
- 실행: `./gradlew test --tests "*OrderServiceTest*"`

---

## 7. 평가 대상

| 항목 | 값 |
| --- | --- |
| 브랜치 | `main` |
| 태그 | `v1.0.0` *(최종 제출 시 생성)* |
| 커밋 | `abc1234` *(제출 직전 `git log -1 --oneline` 값으로 교체)* |

브랜치 전략: `main`(제출) ← `dev`(통합) ← `feature/{도메인}-{기능}`. 커밋 메시지는 `feat: A01 ...` 형식입니다.

---

## 8. 선택 심화 구현

*(구현하지 않았으면 이 절에 "구현하지 않음"이라고 적습니다.)*

| 항목 | 구현 여부 | 코드 위치 | 검증 방법 |
| --- | --- | --- | --- |
| 업무 규칙 자동 테스트 | ⬜ | `src/test/.../OrderServiceTest` | `./gradlew test --tests "*OrderServiceTest*"` |
| 실제 DB 저장·롤백 테스트 | ⬜ | `src/test/.../OrderRollbackTest` | H2 프로파일로 `./gradlew test --tests "*Rollback*"` |
| Redis 메뉴 조회 캐시 (A06/A19 중 택1) | ⬜ | `MenuCacheConfig` | TTL __초, `redis-cli TTL menus:all`로 확인 |
| 메뉴 페이징 `GET /api/v1/menus/page` | ⬜ | `MenuController.page` | `.http` 파일의 페이징 요청 |
| RestClient 외부 조회 `GET /api/v1/reference/posts/{postId}` | ⬜ | `ReferenceController` | `./gradlew test --tests "*Reference*"` |