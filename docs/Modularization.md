프로젝트는 다음과 같은 모듈 구조를 따른다.

```
app
domain
core:data
core:network
core:datastore
core:ui
core:designsystem
feature:auth
feature:home
feature:upload
feature:mypage
build-logic
```

## 각 모듈의 역할 정의

### app 모듈

- 애플리케이션 진입점
- Hilt 엔트리포인트 정의
- 전역 Navigation 그래프 관리 (Navigation3)
- feature 모듈 바인딩

app 모듈은 **비즈니스 로직을 포함하지 않는다.**

### feature 모듈

각 feature 모듈은 **화면 및 사용자 플로우 단위**로 분리된다.  
모든 feature 모듈은 다음 요소를 포함한다.

- Compose UI
- ViewModel
- Navigation Route 정의

### domain 모듈

- 순수 Kotlin 모듈
- 비즈니스 모델 및 Repository 인터페이스 정의
- Android 프레임워크 의존성 없음

#### 포함 요소
- Domain Model
- Repository 인터페이스
- 비즈니스 규칙

### core:data 모듈

- 외부 데이터 소스 접근 책임
- Retrofit2 + OkHttp 기반 네트워크 통신
- Repository 구현체 제공

domain 계층과 feature 계층에는 영향을 주지 않는다.

### core:ui 모듈

- 공용 UI 컴포넌트 정의
- 버튼, 리스트 아이템, 로딩 UI 등 재사용 가능한 Composable 포함

### core:designsystem 모듈

- 색상, 타이포그래피, spacing 등 디자인 토큰 정의
- feature 모듈에서는 디자인 값을 직접 정의하지 않는다.

## 의존성 관리

- 모든 모듈은 **build-logic 기반 커스텀 컨벤션 플러그인**을 사용하여 설정
- 모듈별 의존성 중복 제거
- Gradle 설정의 일관성 유지