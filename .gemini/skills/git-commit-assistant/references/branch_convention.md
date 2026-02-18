# Branch Naming Convention

The project uses the following branch naming structure:
`<type>/#<issue-number>-<description>`

## Types
- `feature`: 새로운 기능 개발
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `docs`: 문서 수정
- `chore`: 설정 및 빌드 관련

## Examples
- `feature/#10-login-ui`
- `fix/#105-token-leak`
- `refactor/#88-mypage-vm`

## Automated Extraction
When preparing a commit, look for the `#<number>` in the current branch name to automatically fill the issue number in the commit message.
