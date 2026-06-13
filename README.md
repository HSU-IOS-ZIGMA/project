# Z-igma

> 약속 장소 후보를 함께 추가하고, 참여자 투표로 최종 장소를 확정하는 약속 조율 서비스입니다.

이 저장소는 Z-igma 프로젝트의 **iOS 앱 코드**와 **Spring Boot 백엔드 서버 코드**를 함께 관리하는 통합 저장소입니다.

프로젝트의 전체 소개, 주요 기능, 화면 흐름 등은 포트폴리오 README에 정리되어 있으며, 이 README는 저장소 구조와 실행 위치를 빠르게 파악하기 위한 안내 문서입니다.

---

## 저장소 구조

```text
Z-igma
├── ios
│   └── Zigma
│       ├── Zigma.xcodeproj
│       ├── Zigma
│       │   ├── App
│       │   ├── Core
│       │   ├── Models
│       │   ├── Services
│       │   ├── Views
│       │   └── Resources
│       └── project.yml
│
└── BE
    ├── src
    │   ├── main
    │   │   ├── java/org/hansung/zigma
    │   │   └── resources
    │   └── test
    ├── build.gradle
    ├── settings.gradle
    └── gradlew
```

---

## 디렉터리 설명

| 경로 | 설명 |
| --- | --- |
| `ios/Zigma` | SwiftUI 기반 iOS 네이티브 앱 프로젝트입니다. |
| `ios/Zigma/Zigma.xcodeproj` | Xcode에서 실행하는 iOS 프로젝트 파일입니다. |
| `ios/Zigma/Zigma/App` | 앱 진입점, 전역 상태, 로그인 상태 관리 코드입니다. |
| `ios/Zigma/Zigma/Core` | API 클라이언트, 앱 설정, 공통 이벤트 코드입니다. |
| `ios/Zigma/Zigma/Models` | 약속, 후보 장소, 댓글, 응답 모델입니다. |
| `ios/Zigma/Zigma/Services` | 백엔드 API 및 WebSocket 연동 로직입니다. |
| `ios/Zigma/Zigma/Views` | 로그인, 홈, 지도, 투표 현황 등 SwiftUI 화면입니다. |
| `BE` | Spring Boot 기반 백엔드 서버 프로젝트입니다. |
| `BE/src/main/java/org/hansung/zigma/domain` | user, promise, presence, notification, comment 도메인 코드입니다. |
| `BE/src/main/java/org/hansung/zigma/global` | 인증, OAuth2, JWT, 공통 응답, 예외 처리, 설정 코드입니다. |
| `BE/src/main/resources` | 백엔드 설정 파일 위치입니다. |

---

## 실행 방법

### 1. 백엔드 실행

```bash
cd BE
./gradlew bootRun
```

백엔드는 기본적으로 `http://localhost:8080`에서 실행됩니다.

실제 DB, JWT, OAuth2, Web Push 키 값은 `application-secret.properties` 또는 환경변수로 관리합니다.  
민감 정보 파일은 `.gitignore`에 포함되어 있어 저장소에 올리지 않습니다.

### 2. iOS 앱 실행

Xcode에서 아래 프로젝트를 엽니다.

```text
ios/Zigma/Zigma.xcodeproj
```

이후 원하는 iPhone 시뮬레이터를 선택해 실행합니다.

iOS 앱은 기본적으로 로컬 백엔드 서버와 통신하도록 설정되어 있습니다.

```swift
static let apiBaseURL = URL(string: "http://localhost:8080")!
static let webSocketURL = URL(string: "ws://localhost:8080/ws")!
```

---

## iOS와 BE 연동 흐름

```text
iOS App
  │
  ├─ HTTP API
  │   ├─ 약속 생성 / 조회
  │   ├─ 후보 장소 추가 / 삭제
  │   ├─ 투표 / 투표 취소
  │   ├─ 장소 확정 / 재투표
  │   └─ 댓글 / 초대 링크
  │
  ├─ WebSocket STOMP
  │   └─ 약속별 실시간 접속자 Presence
  │
  ▼
Spring Boot Backend
  │
  └─ MySQL
```

---

## Git 관리 참고

다음 파일과 디렉터리는 저장소에 올리지 않습니다.

- `application-secret.properties`
- `.env`, `.env.*`
- `.DS_Store`
- `.idea/`, `.gradle/`
- `BE/build/`
- `xcuserdata/`, `*.xcuserstate`
- `DerivedData/`

