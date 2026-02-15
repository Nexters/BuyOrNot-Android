# Commit Convention

## Commit Message Structure

```
<type>/#<issue-number>: <subject>
```

### Type

| Type       | Description                                                  |
| ---------- | ------------------------------------------------------------ |
| `feat`     | 새로운 기능 추가                                             |
| `fix`      | 버그 수정                                                    |
| `docs`     | 문서 수정                                                    |
| `style`    | 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우            |
| `refactor` | 코드 리팩토링                                                |
| `test`     | 테스트 코드, 리팩토링 테스트 코드 추가                       |
| `chore`    | 빌드 업무 수정, 패키지 매니저 수정                           |
| `add`      | 에셋 추가 등                                                 |
| `ci`       | CI 관련 설정                                                 |
| `build`    | 빌드 관련 파일 수정                                          |
| `perf`     | 성능 개선                                                    |


### Subject

- 제목은 50자를 넘기지 않도록 합니다.
- 첫 글자는 대문자로 작성합니다.
- 마침표 및 특수기호는 사용하지 않습니다.
- 과거시제가 아닌 현재시제로 명령문으로 작성합니다.



### Example

```
feat/#10: 네트워크 에러 공통 Error View 컴포넌트 구현
```
