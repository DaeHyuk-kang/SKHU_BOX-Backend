# SKHUBox 백엔드

성공회대학교 사물함 예약 시스템 백엔드 서버입니다.

---

## 1. 개발환경 사양

### H/W 환경

| 항목 | 최소 | 권장 |
|------|------|------|
| RAM | 8GB | 16GB |
| 디스크 | 5GB | 10GB 이상 |

> Spring Boot 서버, MySQL, Redis, IntelliJ를 동시에 구동할 경우 16GB 권장

### S/W 환경

| 항목 | 버전 |
|------|------|
| Java | 17 (LTS) |
| Spring Boot | 3.4.0 |
| MySQL | 8.0 |
| Redis | 7.x (로컬 설치 or Docker) |
| Gradle | 9.4.0 (Wrapper 포함) |

---

## 2. 로컬 개발환경 구축

### 2-1. 사전 설치

- [JDK 17](https://adoptium.net/)
- [MySQL 8.0](https://dev.mysql.com/downloads/)
- [Redis](https://redis.io/download/) (또는 Docker: `docker run -d -p 6379:6379 redis`)

### 2-2. 환경변수 설정

구동에 필요한 환경변수 목록:

| 변수명 | 필수 여부 | 설명 | 예시 |
|--------|-----------|------|------|
| `DB_URL` | 필수 | MySQL JDBC URL | `jdbc:mysql://localhost:3306/skhubox` |
| `DB_USERNAME` | 필수 | DB 사용자명 | `root` |
| `DB_PASSWORD` | 필수 | DB 비밀번호 | `password` |
| `JWT_SECRET` | 필수 | JWT 서명 키 (32자 이상) | `my-secret-key-at-least-32-characters!!` |
| `MAIL_USERNAME` | 필수 | Gmail 계정 | `example@gmail.com` |
| `MAIL_PASSWORD` | 필수 | Gmail 앱 비밀번호 | `abcd efgh ijkl mnop` |
| `CORS_ALLOWED_ORIGINS` | 선택 | 허용할 프론트엔드 주소 (기본값: `http://localhost:3000,http://localhost:5173`) | `http://localhost:3000` |
| `PASSWORD_RESET_URL` | 선택 | 비밀번호 재설정 링크 (이메일에 포함, 미설정 시 코드 직접 입력 안내) | `https://your-domain.com/reset-password` |

> **Redis 연결 설정:** 로컬 개발 환경에서는 `localhost:6379`를 기본값으로 사용하므로 별도 환경변수가 필요 없습니다. 포트나 호스트를 변경해야 할 경우 `application.properties`에 `spring.data.redis.host`, `spring.data.redis.port`를 직접 추가하세요.

#### IntelliJ에서 설정하는 방법

1. 상단 메뉴 `Run` → `Edit Configurations`
2. 실행 구성 선택 → `Environment variables` 항목 클릭
3. 아래 형식으로 입력:

```
DB_URL=jdbc:mysql://localhost:3306/skhubox;DB_USERNAME=root;DB_PASSWORD=password;JWT_SECRET=my-secret-key-at-least-32-characters!!;MAIL_USERNAME=example@gmail.com;MAIL_PASSWORD=앱비밀번호;CORS_ALLOWED_ORIGINS=http://localhost:3000
```

#### OS 환경변수로 설정하는 방법 (macOS/Linux)

```bash
export DB_URL=jdbc:mysql://localhost:3306/skhubox
export DB_USERNAME=root
export DB_PASSWORD=password
export JWT_SECRET=my-secret-key-at-least-32-characters!!
export MAIL_USERNAME=example@gmail.com
export MAIL_PASSWORD=앱비밀번호
export CORS_ALLOWED_ORIGINS=http://localhost:3000
```

#### OS 환경변수로 설정하는 방법 (Windows PowerShell)

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/skhubox"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="password"
$env:JWT_SECRET="my-secret-key-at-least-32-characters!!"
$env:MAIL_USERNAME="example@gmail.com"
$env:MAIL_PASSWORD="앱비밀번호"
$env:CORS_ALLOWED_ORIGINS="http://localhost:3000"
```

### 2-3. DB 초기 설정

MySQL에서 데이터베이스 생성:

```sql
CREATE DATABASE skhubox CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2-4. 서버 구동

```bash
# macOS / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

서버가 정상 실행되면 아래 주소에서 Swagger UI 접근 가능:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 3. 역할 변경 API (아키텍처 검증용)

특정 사용자의 권한을 `ADMIN` 또는 `USER`로 변경하는 임시 API입니다.  
**관리자 JWT 토큰**이 필요합니다.

- **엔드포인트:** `PATCH /api/admin/users/role`
- **권한:** ADMIN만 호출 가능

### Request Body

```json
{
  "targetStudentNumber": "202011111",
  "role": "ADMIN"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `targetStudentNumber` | String | 권한을 변경할 대상 학번 |
| `role` | String | `ADMIN` 또는 `USER` |

### curl 예시

```bash
curl -X PATCH http://localhost:8080/api/admin/users/role \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {ACCESS_TOKEN}" \
  -d '{"targetStudentNumber": "202011111", "role": "ADMIN"}'
```

### PowerShell 예시

```powershell
Invoke-RestMethod -Method Patch `
  -Uri "http://localhost:8080/api/admin/users/role" `
  -Headers @{
    "Content-Type"  = "application/json"
    "Authorization" = "Bearer {ACCESS_TOKEN}"
  } `
  -Body '{"targetStudentNumber": "202011111", "role": "ADMIN"}'
```

### 사용 절차

1. `POST /api/auth/login` 으로 관리자 계정 로그인 → `accessToken` 획득
2. 위 API 호출 시 `Authorization: Bearer {accessToken}` 헤더에 포함
3. 응답 예시:
```json
{
  "success": true,
  "message": "ADMIN 권한으로 변경되었습니다."
}
```

---

## 4. 저장소 구성

```
skhubox/
├── src/
│   ├── main/
│   │   ├── java/          # 소스 코드
│   │   └── resources/     # application.properties (로컬 전용, gitignore)
│   └── test/
├── gradle/wrapper/        # Gradle Wrapper (gradle-wrapper.jar 포함)
├── build.gradle
├── .gitignore
└── README.md
```

### gitignore 적용 항목

| 항목 | 이유 |
|------|------|
| `build/` | 빌드 생성물 |
| `.gradle/` | Gradle 캐시 |
| `application.properties` | DB/JWT 등 민감정보 (패턴 적용) |
| `.env`, `.env.local` | 환경변수 파일 |
| `*.pem` | 인증서 파일 |

> `src/test/resources/application.properties`는 H2 인메모리 DB를 사용하는 테스트 전용 설정으로 민감정보가 없어 저장소에 포함되어 있습니다.

---

## 5. 주요 기술 구현

### Redis 분산락 (동시성 제어)

사물함 예약 시 동시 요청으로 인한 중복 예약을 방지하기 위해 Redis 분산락을 사용합니다.

- 구현 위치: `LockerReservationServiceImpl.reserveLocker()`
- 방식: `SET NX EX` (`setIfAbsent`) — 락 획득 실패 시 즉시 예외 반환
- 락 키: `lock:locker:{lockerId}` / TTL: 5초

```
요청 A ──► Redis SETNX lock:locker:1 ──► 성공 → 예약 진행
요청 B ──► Redis SETNX lock:locker:1 ──► 실패 → 409 Conflict 반환
```

### JPA Fetch Join (N+1 최적화)

연관 엔티티(`locker`, `user`) 접근 시 발생하는 N+1 문제를 `JOIN FETCH`로 해결합니다.

- 적용 위치: `LockerReservationRepository`, `ComplaintRepository`
- 예약 조회, 만료 스케줄러, 관리자 사물함 목록 등 주요 쿼리에 적용

```sql
-- 적용 전: 예약 100건 조회 시 → 201번 쿼리
SELECT * FROM locker_reservations WHERE status = 'ACTIVE'
SELECT * FROM lockers WHERE id = ?  -- 100번 반복
SELECT * FROM users WHERE id = ?    -- 100번 반복

-- 적용 후: 1번 쿼리
SELECT r.*, l.*, u.*
FROM locker_reservations r
JOIN lockers l ON r.locker_id = l.id
LEFT JOIN users u ON r.user_id = u.id
WHERE r.status = 'ACTIVE'
```
