#!/usr/bin/env python3
"""Claude가 커밋하거나 PR을 만들기 전에, 이번 변경분에서 추가·수정된 주석을 돌려줘 규칙대로 다시 보게 한다.

PreToolUse(Bash) 훅. 명령이 `git commit`·`gh pr create`가 아니면 그대로 통과시킨다.
검토를 마친 명령은 `COMMENT_CHECK=reviewed`를 앞에 붙여 다시 실행하면 통과한다.
"""

from __future__ import annotations

import json
import re
import subprocess
import sys
from dataclasses import dataclass

REVIEWED_MARKER = "COMMENT_CHECK=reviewed"
SOURCE_SUFFIXES = (".kt", ".kts", ".java", ".xml")
MAX_LISTED_COMMENTS = 80

COMMIT_PATTERN = re.compile(r"(?:^|[;&|(]|\s)git\s+(?:-c\s+\S+\s+)*commit\b")
PR_CREATE_PATTERN = re.compile(r"(?:^|[;&|(]|\s)gh\s+pr\s+create\b")
PR_BASE_PATTERN = re.compile(r"(?:--base|-B)[=\s]+[\"']?([^\s\"']+)")
HUNK_PATTERN = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,\d+)? @@")

# 경위·이력은 커밋 메시지와 PR이 담는다. 주석에 남으면 코드가 바뀐 뒤에도 사실처럼 읽힌다.
HISTORY_HINTS = [
    (re.compile(r"기존(?:에는|엔|의|처럼)?"), "경위"),
    (re.compile(r"이전(?:에는|엔|\s*버전)"), "경위"),
    (re.compile(r"원래(?:는|대로)?"), "경위"),
    (re.compile(r"예전"), "경위"),
    (re.compile(r"처음에는"), "경위"),
    (re.compile(r"임시로"), "경위"),
    (re.compile(r"(?:바꿨|바꾸었|변경했|수정했|고쳤|옮겼|추가했|제거했|삭제했|없앴|되돌렸)"), "경위"),
    (re.compile(r"리뷰\s*(?:반영|에서|지적)"), "경위"),
    (re.compile(r"PR\s*#?\d+|\(#\d+\)|이슈\s*#\d+"), "경위"),
    (re.compile(r"\d+(?:\.\d+)?\s*(?:dp|sp|px|ms|초|분|%)"), "수치 재기재"),
]


@dataclass
class ChangedComment:
    path: str
    line: int
    text: str

    @property
    def hints(self) -> list[str]:
        return sorted({label for pattern, label in HISTORY_HINTS if pattern.search(self.text)})


def comment_text(source_line: str) -> str | None:
    """코드 한 줄에서 주석 부분만 꺼낸다. 주석이 없으면 None."""
    stripped = source_line.strip()
    if not stripped:
        return None
    if stripped.startswith(("//", "/*", "*", "<!--")):
        return stripped
    # URL의 "://"는 주석이 아니다.
    match = re.search(r"(?<![:\"'])//\s", source_line)
    if match and source_line.count('"', 0, match.start()) % 2 == 0:
        return source_line[match.start():].strip()
    return None


def changed_comments(diff: str) -> list[ChangedComment]:
    comments: list[ChangedComment] = []
    path: str | None = None
    line_no = 0
    for raw in diff.splitlines():
        if raw.startswith("+++ "):
            target = raw[4:]
            path = target[2:] if target.startswith("b/") else None
            continue
        if raw.startswith("--- "):
            continue
        hunk = HUNK_PATTERN.match(raw)
        if hunk:
            line_no = int(hunk.group(1))
            continue
        if path is None or not path.endswith(SOURCE_SUFFIXES):
            continue
        if raw.startswith("+"):
            text = comment_text(raw[1:])
            if text is not None and text not in ("*", "*/", "/**", "/*"):
                comments.append(ChangedComment(path=path, line=line_no, text=text))
            line_no += 1
        elif not raw.startswith("-"):
            line_no += 1
    return comments


def git(*args: str, cwd: str) -> str:
    result = subprocess.run(["git", *args], cwd=cwd, capture_output=True, text=True)
    return result.stdout if result.returncode == 0 else ""


def diff_for(command: str, cwd: str) -> str:
    if PR_CREATE_PATTERN.search(command):
        base_match = PR_BASE_PATTERN.search(command)
        base = base_match.group(1) if base_match else "develop"
        return git("diff", "-U0", f"origin/{base}...HEAD", cwd=cwd)
    # pre-commit 훅이 수정된 추적 파일을 모두 다시 스테이징하므로, 스테이징 여부와 상관없이 HEAD 대비 변경을 본다.
    return git("diff", "-U0", "HEAD", cwd=cwd)


def review_request(comments: list[ChangedComment]) -> str:
    listed = comments[:MAX_LISTED_COMMENTS]
    lines = []
    for comment in listed:
        mark = f"  ⚠ {', '.join(comment.hints)}" if comment.hints else ""
        lines.append(f"- {comment.path}:{comment.line}{mark}\n    {comment.text}")
    if len(comments) > len(listed):
        lines.append(f"- … 외 {len(comments) - len(listed)}개")
    return "\n".join(
        [
            "이번 변경분에서 추가·수정된 주석을 아래 규칙으로 검토하세요.",
            "",
            "규칙",
            "- 주석에는 지금 무엇이 있고 왜 그런지만 쓴다.",
            "- 바꾼 경위·이력(기존에는, 이전에는, PR #n 리뷰, 임시로, ~로 바꿨다)은 커밋 메시지와 PR에 쓴다.",
            "- 코드만 봐도 아는 '무엇을 하는지'는 쓰지 않는다. 코드만으로 알 수 없는 이유·제약·외부 계약만 쓴다.",
            "- 이름 붙은 상수·계산식에 있는 수치를 주석에 다시 적지 않는다.",
            "",
            f"변경된 주석 {len(comments)}개 (⚠는 규칙 위반 의심 표현)",
            *lines,
            "",
            "규칙에 어긋나는 주석은 고치거나 지운 뒤 다시 실행하세요.",
            f"모두 확인했고 고칠 것이 없으면 같은 명령 앞에 `{REVIEWED_MARKER} `를 붙여 다시 실행하세요.",
        ]
    )


def deny(reason: str) -> None:
    print(
        json.dumps(
            {
                "hookSpecificOutput": {
                    "hookEventName": "PreToolUse",
                    "permissionDecision": "deny",
                    "permissionDecisionReason": reason,
                }
            },
            ensure_ascii=False,
        )
    )


def main() -> int:
    try:
        payload = json.load(sys.stdin)
    except json.JSONDecodeError:
        return 0
    command = (payload.get("tool_input") or {}).get("command") or ""
    if REVIEWED_MARKER in command:
        return 0
    if not (COMMIT_PATTERN.search(command) or PR_CREATE_PATTERN.search(command)):
        return 0
    cwd = payload.get("cwd") or "."
    comments = changed_comments(diff_for(command, cwd))
    if comments:
        deny(review_request(comments))
    return 0


if __name__ == "__main__":
    sys.exit(main())
