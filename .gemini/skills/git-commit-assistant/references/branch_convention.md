# Branch Naming Convention

The project uses the following branch naming structure:
`<type>/#<issue-number>-<description>`

## Types
- `feat`: 새로운 기능 개발
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `docs`: 문서 수정
- `chore`: 설정 및 빌드 관련
- `design`: 디자인 작업

## Examples
- `feat/#38-upload-api`
- `fix/#105-token-leak`
- `refactor/#88-mypage-vm`

## Automated Extraction
When preparing a commit, look for the `<type>/#<number>` in the current branch name to automatically format the commit message as `<type>/#<number>: <subject>`.
