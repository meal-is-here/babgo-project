# babgo-project
BabGO — AI-powered food delivery project by Team Meal Is Here

## 프로젝트 구조 & 컨벤션

### 📂 폴더 구조

```text
com.babgo.[domain]
├─ application
│  ├─ facade                # 유스케이스(트랜잭션 경계, 조합/흐름)
│  └─ info                  # 유스케이스 I/O DTO (내부 교환 전용)
│
├─ controller
│  ├─ ApiController         # HTTP 경계 (검증, 변환, 호출 위임)
│  ├─ request               # HTTP 요청 DTO
│  └─ response              # HTTP 응답 DTO
│
├─ domain
│  ├─ model                 # 엔티티/값객체 (불변식, 상태 전이)
│  ├─ service               # 도메인 서비스(순수 규칙/계산/협력)
│  ├─ event                 # 도메인 이벤트 (OrderCreated 등)
│  └─ repository            # Port 인터페이스 (도메인 관점)
│
└─ repository
   ├─ JpaRepository         # Spring Data JPA 인터페이스
   └─ repositoryImpl        # Port 구현체(infra adapter, JPA↔Domain 매핑)
```

<details>
<summary>📖 규칙 요약 및 코드 컨벤션</summary>


---

### 📖 규칙 요약

| 영역 | 설명 | 허용 | 제한 |
|------|------|------|------|
| **controller** | HTTP 요청/응답 처리 계층. 변환·검증·위임만 담당 | - DTO 변환을 통한 Facade 호출<br>- ApiController 사용 | - 비즈니스 로직 직접 호출 금지<br>- Facade 외 계층 의존 금지 |
| **application** | 유스케이스 조합 계층. 트랜잭션 경계, 도메인 서비스 호출 | - Facade<br>- Info DTO<br>- @Transactional<br>- 도메인 서비스/엔티티 직접 소통 허용<br>  (도메인 전용 DTO 미사용) | - 비즈니스 규칙 직접 수행 금지 |
| **domain** | 핵심 로직 계층. 엔티티/값객체/도메인 서비스 | - JPA 영속성 어노테이션 사용 허용<br>- 도메인 이벤트 발행<br>- VO/Enum 사용<br>- 생성자 불변성 검증<br>- 상태 변경은 도메인 메서드로만 수행 | - DTO 의존 금지<br>- Setter 사용 금지<br>- 외부 인프라 직접 호출 금지 |
| **repository** | 인프라 계층. DB·외부 연동 | - JPA<br>- 구현체(Impl)에서 데이터 저장/조회 처리 | - 도메인에서 JPA 직접 의존 금지 |
| **global** | 전역 공통 기능 (Exception, Util 등) | - CustomException<br>- ErrorType<br>- 공통 유틸 | - 도메인 특화 로직 |


---

### 📑 네이밍 규칙

| 구분 | 규칙 | 예시 |
|------|------|------|
| **변수/메서드** | `camelCase` | `createUser`, `likeCount` |
| **클래스/인터페이스** | `PascalCase` | `UserServiceImpl`, `UserRepository`, `ApiResponse` |
| **단건 조회 메서드** | `getUser{}` | `getUserById` |
| **다중 조회 메서드** | `get{ }s` | `getUsers`, `getOrders` |
| **전체 조회 메서드** | `getAll{}` | `getAllProducts` |
| **생성 메서드** | `create{}` | `createUser` |
| **수정 메서드** | `update{}` | `updateOrder` |
| **삭제 메서드** | `delete{}` | `deleteComment` |
| **취소 메서드** | `cancel{}` | `cancelPayment` |
| **DB 관련** | JPA 네이밍 규칙 준수 | `findByEmail`, `existsByNickname` |

---

### 📌 DTO 규칙

- **Controller DTO** → `Request`, `Response`  
- **Application DTO** → `Info`  
- **Entity 클래스** → 도메인명 그대로 사용  
- **Repository 네이밍**  
  - 도메인: `UserRepository`  
  - JPA: `UserJpaRepository`  
  - 구현체: `UserRepositoryImpl`

---

### 📌 추가 규칙

- **공통 응답**: `ApiResponse.success()`, 예외는 `CustomException`으로 처리  
- **비밀 키 관리**: `.env` 사용 (`.gitignore` 등록 필수)  
- **코드 스타일**:  
  - 어노테이션은 길이 짧은 것부터 정렬합니다.
  - `setter`, `@Data` 지양합니다.
  - DTO → 내부(static) 클래스 사용, 모든 필드는 final로 선언하여 불변성을 보장합니다.
- **엔티티 / 값 객체**
  - **생성자 불변성**: 생성자에서 모든 필드 검증을 수행하여.  
     비즈니스 로직에서 객체를 호출할 때는 이미 완전하고 일관된 상태임을 보장합니다.
  - 팩토리 메서드(`of`)를 통해서만 생성을 허용합니다.
  - Setter 사용 지양 합니다. 상태 변경은 도메인 메서드를 통해 수행합니다.
</details> 
