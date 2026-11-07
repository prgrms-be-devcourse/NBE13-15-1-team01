# Grids & Circle — 카페 메뉴 관리 서비스

**NBE13-15-1-team01**

로컬 카페 Grids & Circle의 원두 패키지 주문 및 상점 관리 백엔드 프로젝트입니다.

Spring Boot 기반으로 카테고리·메뉴 관리, 주문·배송 묶음·통계 API를 구현하고, 제공된 웹 화면과 연동합니다.

- **주요 기능:** 카테고리·메뉴 CRUD, 주문 관리, 배송 묶음, 통계
- **API:** A01~A24 (총 24개)
- **기술 스택:** Java 21, Spring Boot, MySQL

### 참고 자료

- [API 명세서](https://app.notion.com/p/API-40c15a01205480e6bb31c07909d68298?source=copy_link)
- [기획서](https://app.notion.com/p/40c15a0120548005b624d00c8347f2b2?source=copy_link)
- [ERD](https://app.notion.com/p/ERD-40d15a01205480f5bbf1c2358e38851d?source=copy_link)

---

## 1. 팀원별 담당 작업

### 개인별 담당

| 팀원 | 담당 API | 담당 도메인 및 작업 |
| --- | --- | --- |
| 팀원1 차호준 | A01~A10 | 카테고리·메뉴 CRUD, 초기 설정, 초기 데이터, CORS |
| 팀원2 김진형 | A11~A13, A16, A22, A23 | 주문 관리, 배송일·취소 마감 계산, 트랜잭션 |
| 팀원3 김지원 | A14, A17, A18, A24 | 배송 묶음, 통계, 집계 쿼리, 통합 테스트 |
| 팀원4 한다솔 | A15, A19 | 메뉴 조회 확장, PPT 제작 |
| 팀원5 이제섭 | A20, A21 | 메뉴 검색·판매 여부, 발표·시연, 웹 화면 연결 |

### 공동 구현 작업

| 작업 | 담당자 | 주요 내용 |
| --- | --- | --- |
| DB 설계 및 ERD | 차호준, 김진형 | 차호준: category·menu 테이블 / 김진형: orders·order_item 테이블 |
| 공통 예외 처리 | 차호준 | GlobalExceptionHandler, ErrorResponse, 커스텀 예외 클래스 |
| CORS 및 웹 화면 연결 | 차호준, 이제섭 | CafeCorsConfig 설정 및 화면 연동 확인 |
| API 요청 테스트 | 전원 | 담당 API별 정상·오류 요청 작성 |
| Controller 자동 테스트 | 전원 | 팀 전체 총 6개 테스트 작성 |

---

## 2. 구현 현황

### 기능별 구현 상태

| API | 기능 | 상태 |
| --- | --- | --- |
| A01~A05 | 카테고리 CRUD | ✅ 완료 |
| A06~A10 | 메뉴 CRUD | ✅ 완료 |
| A11~A13 | 주문 접수·목록·상세 조회 | ✅ 완료 |
| A14 | 배송 묶음 | ✅ 완료 |
| A15, A19~A21 | 메뉴 조회·검색·판매 여부 | ✅ 완료 |
| A16, A22, A23 | 주문 취소·메모·재주문 | ✅ 완료 |
| A17, A18, A24 | 접수일별 통계 | ✅ 완료 |

### 미완료 작업

- 선택 심화: 실제 DB 저장·롤백 테스트, RestClient 외부 조회 (8절 참고)

### 미해결 오류

현재 확인된 미해결 오류는 없습니다.

---

## 3. 개발 환경

### 사용 기술 및 버전

| 항목 | 버전 |
| --- | --- |
| Java | 21.0.12 |
| Spring Boot | 4.1.2 |
| Gradle | 9.8.2 |
| MySQL | 8.4.11 |
| IDE | IntelliJ IDEA Ultimate |
| Redis | 7.4 (선택 심화: 메뉴 조회 캐시) |

### DB 설정 및 환경변수

DB 연결 설정은 `src/main/resources/application.yaml`에서 관리합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:cafe_db}?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    defer-datasource-initialization: true

  sql:
    init:
      mode: always

  jackson:
    time-zone: Asia/Seoul
```

| 환경변수 | 설명 | 기본값 |
| --- | --- | --- |
| `DB_HOST` | MySQL 호스트 | `localhost` |
| `DB_PORT` | MySQL 포트 | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `cafe_db` |
| `DB_USERNAME` | DB 사용자 | `root` |
| `DB_PASSWORD` | DB 비밀번호 | 필수 입력 |

`DB_PASSWORD`를 제외한 환경변수는 기본값을 사용합니다.

**IntelliJ에서 설정하기**

1. `실행 → 구성 편집` 메뉴로 이동합니다.
2. 실행 구성의 환경 변수 항목을 엽니다.
3. 다음 형식으로 환경변수를 등록합니다.

```text
DB_PASSWORD=실제_DB_비밀번호
```

**터미널에서 실행하기**

```bash
DB_PASSWORD=실제_DB_비밀번호 ./gradlew bootRun
```

DB 비밀번호 등 민감한 정보는 Git 저장소에 포함하지 않습니다.

---

## 4. 새 DB 준비 및 초기 데이터 등록

애플리케이션 실행 시 테이블은 Hibernate(`ddl-auto: update`)가 자동으로 생성하고, 초기 데이터는 `data.sql`로 등록됩니다.

### 1) 데이터베이스 생성

MySQL에 접속한 후 데이터베이스를 생성합니다.

```sql
CREATE DATABASE cafe_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_bin;
```

### 2) 환경변수 설정

3절의 환경변수 설정 방법에 따라 DB 접속 정보를 등록합니다.

### 3) 애플리케이션 실행

백엔드를 실행하면 다음 순서로 초기화가 진행됩니다.

| 파일·설정 | 역할 |
| --- | --- |
| `ddl-auto: update` (Hibernate) | `categories`, `menus`, `orders`, `order_items` 테이블 생성 |
| `data.sql` | 초기 카테고리 2개 및 메뉴 4개 등록 |

- 테이블 생성: 엔티티 기준으로 없는 테이블만 생성 (이미 있는 테이블과 데이터는 유지)
- 초기 데이터 등록: `INSERT ... WHERE NOT EXISTS` (이미 있는 이름은 건너뜀)

### 4) 초기 데이터 확인

```sql
SELECT * FROM categories;
SELECT * FROM menus;
```

초기 데이터는 다음과 같이 등록됩니다.

- 카테고리: 싱글오리진, 블렌드 (총 2개)
- 메뉴: 총 4개
- 초기 메뉴는 싱글오리진 카테고리에 등록됩니다.

`spring.sql.init.mode: always` 설정으로 애플리케이션을 실행할 때마다 `data.sql`이 실행됩니다. `ddl-auto: update`는 없는 테이블만 만들고 `data.sql`은 이미 있는 데이터를 건너뛰므로, 기존 테이블과 초기 데이터가 중복 생성되지 않습니다.

데이터베이스를 완전히 초기화하려면 다음 명령을 실행한 후 1번부터 다시 진행합니다.

```sql
DROP DATABASE cafe_db;
```

> 데이터베이스 삭제 시 기존 데이터가 모두 사라지므로 주의해야 합니다.

---

## 5. 실행 방법

### 백엔드 실행

프로젝트 루트 디렉터리에서 실행합니다.

```bash
./gradlew bootRun
```

- 서버 주소: `http://127.0.0.1:8080`
- API 기본 경로: `http://127.0.0.1:8080/api/v1`

### 웹 화면 실행

웹 화면 디렉터리로 이동한 후 실행합니다.

```bash
cd web
java -m jdk.httpserver -b 127.0.0.1 -p 8000
```

- 웹 화면 주소: `http://127.0.0.1:8000/`

### 웹 화면과 백엔드 연결

1. 웹 화면에 접속합니다.
2. **연결 설정 → 실제 API 연결**을 선택합니다.
3. API 서버 주소에 다음 주소를 입력합니다.

```text
http://127.0.0.1:8080
```

> API 서버 주소 끝에 `/api/v1`을 추가하지 않습니다. CORS 설정에 맞춰 웹 화면과 백엔드 주소는 `127.0.0.1`로 통일합니다.

---

## 6. 테스트

### 자동 테스트 실행

프로젝트 루트에서 다음 명령어를 실행합니다.

```bash
DB_PASSWORD=실제_DB_비밀번호 ./gradlew test
```

- `CafeSystemApplicationTests`는 실제 MySQL에 접속해 애플리케이션 시작을 확인하므로 `DB_PASSWORD`가 필요합니다. IntelliJ에서 테스트를 실행할 때도 실행 구성에 같은 환경변수를 등록합니다.
- `CafeSystemApplicationTests`를 제외한 테스트는 MySQL 없이 실행됩니다. (아래 명령은 `DB_PASSWORD` 없이 실행 가능)
- Redis 캐시 테스트(`AvailableMenuCacheTest`)는 Redis(`localhost:6379`)가 켜져 있을 때만 실행되고, 꺼져 있으면 실패 대신 건너뜁니다.

테스트 결과 리포트:

```text
build/reports/tests/test/index.html
```

### 테스트 코드 구성

```text
src/test/java/com/kdt/cafesystem
├── CafeSystemApplicationTests.java       # 애플리케이션 시작 확인 (실제 MySQL)
├── controller
│   └── ControllerRequirementTest.java    # 기본 Controller 자동 테스트 6개
├── advanced                              # 선택 심화 테스트
│   ├── OrderBusinessRuleTest.java        # 업무 규칙: 배송일·취소 마감(13:59:59/14:00:00), 재취소, 재주문
│   ├── MenuPageTest.java                 # 별도 메뉴 페이징
│   └── AvailableMenuCacheTest.java       # Redis 메뉴 조회 캐시 (실제 Redis + H2)
├── delivery/service
│   └── DeliveryBatchServiceTest.java     # A14 배송 묶음 합산·정렬
└── statistics/service
    └── StatisticsServiceTest.java        # A17·A18·A24 통계 계산
```

```bash
./gradlew test --tests "com.kdt.cafesystem.controller.*"   # 기본 Controller 테스트 6개
./gradlew test --tests "com.kdt.cafesystem.advanced.*"     # 선택 심화 테스트
```

### 기본 Controller 자동 테스트

팀 전체 총 6개의 Controller 테스트를 `controller/ControllerRequirementTest` 한 파일에 작성합니다.

- `@WebMvcTest`로 실제 `MenuController`·`OrderController`에 HTTP 요청(MockMvc)을 보냅니다.
- Service는 모의 객체(`@MockitoBean`)를 사용하며 실제 DB에는 연결하지 않습니다.
- 각 테스트의 `@DisplayName`에 확인하는 요구사항을 한 문장으로 적습니다.
- 모의 Service 결과만 확인하는 테스트이므로, 실제 업무 규칙은 `advanced/OrderBusinessRuleTest`에서 따로 검증합니다.

| 분류 | 테스트 메서드 | 검증 내용 |
| --- | --- | --- |
| 정상 처리 1 | `Success.createMenu` | A08 메뉴 등록 시 201, `Location: /api/v1/menus/1`, id·categoryId·name·price·description·available 값 확인 |
| 정상 처리 2 | `Success.deleteMenu` | A10 메뉴 삭제 시 204와 빈 본문 확인 |
| 입력 오류 1 | `InvalidInput.createMenu_priceOutOfRange` | A08 가격 0원이면 400, `VALIDATION_ERROR`, 공통 message, `fieldErrors.price` 하나만 반환, Service 호출 없음 |
| 입력 오류 2 | `InvalidInput.createOrder_invalidPostcode` | A11 우편번호가 숫자 5자리가 아니면 400, `VALIDATION_ERROR`, `fieldErrors.postcode` 메시지 확인 |
| 대상 없음 | `NotFoundOrConflict.findOrder_notFound` | A13 없는 주문 조회 시 404, `ORDER_NOT_FOUND`, message, 빈 fieldErrors 확인 |
| 업무 위반 | `NotFoundOrConflict.deleteMenu_inUse` | A10 주문에 사용된 메뉴 삭제 시 409, `MENU_IN_USE`, message, 빈 fieldErrors 확인 |

### API 요청 테스트

테스트 파일: `requests/api-check.http`

파일 상단에 공통 변수를 정의합니다.

```http
@baseUrl = http://127.0.0.1:8080/api/v1
@categoryId = 1
@menuId = 1
@menuId2 = 2
@availabilityMenuId = 4
@email = buyer@example.com
@keyword = Brazil Serra Do Caparaó
```

**테스트 구성**

- 정상 요청: 24개 (A01~A24)
- 오류 요청: 6개

| 상태 코드 | 테스트 내용 |
| --- | --- |
| 400 × 2 | A08 가격 범위 초과 / A14 범위 밖 배송일 (2000-01-01~2099-12-31 밖) |
| 404 × 1 | A07 존재하지 않는 menuId 조회 |
| 409 × 3 | A03 카테고리명 중복 / A05 메뉴가 들어 있는 카테고리 삭제 / A10 주문에 사용된 메뉴 삭제 |

**실행 방법**

1. 초기 데이터가 등록된 상태에서 백엔드를 실행합니다.
2. `requests/api-check.http` 파일을 위에서 아래 순서로 한 번에 실행합니다. (IntelliJ: Run All Requests in File)
3. A03·A08에서 만든 카테고리·메뉴의 ID와 A11 주문의 ID·배송일·접수일은 응답에서 자동으로 저장되어 이후 요청에 사용됩니다. 직접 입력할 필요가 없습니다.
4. 각 요청의 응답 상태 코드와 결과를 확인합니다.

- 초기 데이터의 ID가 다른 DB에서 실행할 때는 파일 상단의 `@categoryId`, `@menuId`, `@menuId2`, `@availabilityMenuId`만 실제 ID로 바꿉니다.
- A03·A08에서 만든 카테고리·메뉴는 A05·A10에서 삭제하므로 여러 번 실행해도 됩니다.

각 요청에는 준비 데이터, 기대 상태 코드, 비교할 응답 값을 주석으로 작성합니다.

### 시각 경계값 테스트

배송일 및 주문 취소 마감 시간은 `Clock`을 주입하여 테스트합니다. PC나 서버의 실제 시계를 변경하지 않고 특정 시각을 재현할 수 있습니다.

**운영 코드**

- `ClockConfig`에서 `Asia/Seoul` 기준 `Clock` 빈을 등록합니다.
- `OrderService`는 주입받은 `Clock`으로 접수 시각을 구하고(초 단위 절사), `ShippingPolicy`가 배송일과 취소 마감(14:00)을 계산합니다.

**테스트 코드** (`advanced/OrderBusinessRuleTest`)

- 테스트마다 `Clock.fixed(...)`로 서울 시각을 고정한 실제 `OrderService`를 만들고, 실제 `OrderController`에 HTTP 요청을 보냅니다.
- Repository만 모의 객체이므로 DB 없이 실행됩니다.

| 테스트 | 고정 시각 | 확인 내용 |
| --- | --- | --- |
| `create_shippingDateBoundary` | 2026-11-05 13:59:59 / 13:59:59.700 | A11 주문 접수 201, 배송일 2026-11-05(당일), createdAt `2026-11-05T13:59:59+09:00` (소수점 초 버림) |
| `create_shippingDateBoundary` | 2026-11-05 14:00:00 / 14:00:01 | 배송일 2026-11-06(다음 날) |
| `create_shippingDateBoundary` | 2026-12-31 14:00:00 | 배송일 2027-01-01(다음 해) |
| `cancel_deadlineBoundary` | 배송일 13:59:59 / 14:00:00 | A16 취소가 각각 200 `CANCELLED` / 409 `ORDER_NOT_CANCELLABLE`, 실패 시 주문은 `PLACED` 유지 |
| `cancel_againAfterDeadline_keepsOriginal` | 09:00 취소 → 다음 날 15:30 재취소 | 200, 최초 cancelledAt·createdAt·배송일·총액 유지 |
| `reorder_usesCurrentPrice_keepsOriginal` | 2026-11-05 15:00 | A23 재주문 201, 새 주문은 현재 가격(6000원), 원본 주문은 5000원·10000원 유지 |

**테스트 실행**

```bash
./gradlew test --tests "*OrderBusinessRuleTest"
```

---

## 7. 평가 대상

| 항목 | 값 |
| --- | --- |
| 제출 브랜치 | `main` |
| 제출 태그 | `v1.0.0` |
| 제출 커밋 | 제출 직전 최신 커밋 해시로 갱신 |

### 브랜치 전략

```text
main
 └── dev
      └── feature/{도메인}-{기능}
```

- `main`: 최종 제출 및 배포 기준 브랜치
- `dev`: 팀 작업 통합 브랜치
- `feature`: 기능별 작업 브랜치

### 커밋 메시지 규칙

기능 단위로 커밋하며 다음 형식을 사용합니다.

```text
feat: A01 카테고리 목록 조회
fix: A09 메뉴 수정 오류 해결
docs: README 작성
```

최종 제출 시 `main` 브랜치의 최신 커밋 해시를 확인하고 태그를 생성합니다.

```bash
git log -1 --oneline
git tag v1.0.0
```

---

## 8. 선택 심화 구현

선택 심화 구현 항목과 진행 상태를 정리합니다.

| 항목 | 구현 여부 | 코드 위치 | 검증 방법 |
| --- | --- | --- | --- |
| 업무 규칙 자동 테스트 | ✅ 완료 | `src/test/.../advanced/OrderBusinessRuleTest` | `./gradlew test --tests "*OrderBusinessRuleTest"` |
| 실제 DB 저장·롤백 테스트 | ⬜ 미구현 | `src/test/.../OrderRollbackTest` | H2 프로파일로 테스트 실행 |
| Redis 메뉴 조회 캐시 (A19 판매 중 메뉴) | ✅ 완료 | `menu/service/AvailableMenuCache`, `MenuService.findAvailable` | `./gradlew test --tests "*AvailableMenuCacheTest"` / `redis-cli TTL cafe:menus:available` |
| 메뉴 페이징 | ✅ 완료 | `MenuController.findPage`, `MenuService.list`, `CafePageConfig` | `./gradlew test --tests "*MenuPageTest"` / `.http` 파일의 페이징 요청 |
| RestClient 외부 조회 | ⬜ 미구현 | `ReferenceController` | `./gradlew test --tests "*Reference*"` |

**선택 심화 구현 API 예시**

- 메뉴 페이징: `GET /api/v1/menus/page`
- 외부 게시글 조회: `GET /api/v1/reference/posts/{postId}`

### 메뉴 페이징

| 항목 | 내용 |
| --- | --- |
| 대상 API | `GET /api/v1/menus/page?page=0&size=10` |
| 기본값 | `page`·`size`가 없으면 0페이지, 10개 |
| 최대 크기 | `size`가 50을 넘으면 50으로 제한 |
| 정렬 | 요청의 `sort`와 관계없이 id 오름차순 고정 |
| 응답 | `content`, `number`, `size`, `totalElements`, `totalPages` |

### Redis 메뉴 조회 캐시 (A19)

| 항목 | 내용 |
| --- | --- |
| 대상 API | A19 `GET /api/v1/menus/available` |
| 키 | `cafe:menus:available` |
| 값 | A19 응답과 같은 JSON 배열 (String) |
| TTL | **60초** |
| 캐시 삭제 | A08 등록, A09 수정, A10 삭제, A21 판매 여부 변경이 성공(commit)한 뒤 |
| Redis 장애 시 | 캐시 없이 DB에서 조회 (A19는 정상 응답) |
| 접속 환경변수 | `REDIS_HOST` (기본 `localhost`), `REDIS_PORT` (기본 `6379`) |

**Redis 실행**

`kdt-redis` 컨테이너가 이미 있으면 그것을 켜고, 없으면 새로 만듭니다.

```bash
docker start kdt-redis 2>/dev/null || docker run -d --name kdt-redis -p 127.0.0.1:6379:6379 redis:7.4
```

기존 `kdt-redis`가 6379가 아닌 다른 포트에 연결되어 있다면 `REDIS_PORT` 환경변수로 그 포트를 지정합니다.

**자동 테스트** (실제 Redis + H2, Redis가 꺼져 있으면 건너뜀)

```bash
./gradlew test --tests "*AvailableMenuCacheTest"
```

**수동 확인** (`requests/redis-menu-cache.http`를 위에서부터 실행)

```bash
docker exec kdt-redis redis-cli GET cafe:menus:available
docker exec kdt-redis redis-cli TTL cafe:menus:available
```

- 캐시가 유효한 동안 다시 조회하면 실행 SQL 로그(`org.hibernate.SQL`)에 `menus` select가 찍히지 않습니다.
- 메뉴 변경 직후 `EXISTS cafe:menus:available`이 `0`이고, 다음 조회에 변경 결과가 반영됩니다.

구현한 항목은 구현 여부를 변경하고 실제 코드 위치와 검증 결과를 함께 기록합니다.
