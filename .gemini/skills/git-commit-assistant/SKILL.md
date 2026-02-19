---
name: git-commit-assistant
description: Automates git commits according to the BuyOrNot-Android project convention. Use when the user wants to commit changes, wrap up a task, or prepare a pull request.
---

# Git Commit Assistant

## Overview

This skill ensures all git commits follow the structured format: `<type>/#<issue-number>: <subject>`. It helps identify the correct commit type, format the subject according to rules (50 chars, imperative, capitalized), and include the mandatory issue number.

## Workflow

1. **Analyze Changes**: Run `git status` and `git diff` to understand what has changed.
2. **Identify Type**: Determine the appropriate type (feat, fix, refactor, etc.) based on [convention.md](references/convention.md).
3. **Get Issue Number**:
    - Check current branch name (e.g., `feature/#123-login` -> 123) following [branch_convention.md](references/branch_convention.md).
    - If not found, ask the user: "What is the issue number for this commit?"
4. **Draft Subject**:
    - Summarize changes in Korean (or English if preferred by user) under 50 characters.
    - Start with a capital letter.
    - Use imperative mood (e.g., "구현", "수정", "추가").
5. **Propose Command**:
    - Show the full command: `git commit -m "<type>/#<issue-number>: <subject>"`
    - Wait for user confirmation before executing.

## Examples

- **New Feature**: `feat/#42: 카카오 로그인 연동 구현`
- **Bug Fix**: `fix/#105: 로그아웃 시 토큰이 삭제되지 않는 버그 수정`
- **Refactor**: `refactor/#88: MyPageViewModel 로직 분리 및 최적화`

## Reference
- **[Commit Convention](references/convention.md)**: Full list of types and formatting rules.
- **[Branch Naming Convention](references/branch_convention.md)**: Rules for branch names and issue number extraction.
