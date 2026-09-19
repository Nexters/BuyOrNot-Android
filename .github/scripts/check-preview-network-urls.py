#!/usr/bin/env python3
"""@Preview 함수 본문에 네트워크 URL이 들어가는 것을 막는다 (#153).

프리뷰가 외부 URL을 때리면 스크린샷 비교 CI의 렌더 결과가 네트워크 상태에 좌우된다.
아무도 UI를 건드리지 않은 PR에서 diff가 떠도 원인이 코드가 아니라서 추적하기 어렵다.
프리뷰는 PreviewImages의 로컬 리소스 URI만 써야 한다.

의도적으로 좁게 잡는다. `@Preview`가 붙은 함수의 본문만 보므로, 프로덕션 코드의
실제 서비스 URL(WebViewNavigation의 약관 링크 등)은 대상이 아니다.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

PREVIEW_ANNOTATION = re.compile(r"^\s*@Preview\b")
FUN_START = re.compile(r"^\s*(?:private\s+|internal\s+|public\s+)?fun\s")
NETWORK_URL = re.compile(r'"https?://')

# 스크린샷 테스트를 적용하지 않는 경로.
EXCLUDED_DIRS = ("/build/", "/src/test/", "/src/androidTest/")


def preview_function_bodies(lines: list[str]):
    """(시작줄번호, 본문 줄들) 을 @Preview 함수마다 내놓는다.

    중괄호 깊이로 본문 끝을 찾는다. 프리뷰는 어노테이션과 본문이 붙어 있는 단순한
    형태라 파서를 세울 필요는 없다.
    """
    i = 0
    n = len(lines)
    while i < n:
        if not PREVIEW_ANNOTATION.match(lines[i]):
            i += 1
            continue

        # @Preview 뒤에 다른 어노테이션이나 @Composable 이 이어질 수 있다.
        j = i
        while j < n and not FUN_START.match(lines[j]):
            j += 1
        if j >= n:
            return

        start = j
        depth = 0
        opened = False
        while j < n:
            depth += lines[j].count("{") - lines[j].count("}")
            if "{" in lines[j]:
                opened = True
            if opened and depth <= 0:
                break
            j += 1

        yield start, lines[start : j + 1]
        i = j + 1


def main() -> int:
    root = Path(__file__).resolve().parents[2]
    violations: list[str] = []

    for path in root.rglob("*.kt"):
        rel = "/" + str(path.relative_to(root))
        if any(part in rel for part in EXCLUDED_DIRS):
            continue

        lines = path.read_text(encoding="utf-8").splitlines()
        if not any(PREVIEW_ANNOTATION.match(line) for line in lines):
            continue

        for start, body in preview_function_bodies(lines):
            for offset, line in enumerate(body):
                if NETWORK_URL.search(line):
                    violations.append(
                        f"{rel.lstrip('/')}:{start + offset + 1}: {line.strip()}"
                    )

    if violations:
        print("@Preview 본문에 네트워크 URL이 있습니다:", file=sys.stderr)
        for v in violations:
            print(f"  {v}", file=sys.stderr)
        print(
            "\n프리뷰는 PreviewImages의 로컬 리소스만 사용해야 합니다.\n"
            "네트워크 이미지는 렌더 결과를 네트워크 상태에 좌우되게 만들어,\n"
            "UI를 건드리지 않은 PR에서도 스크린샷 diff를 만듭니다.",
            file=sys.stderr,
        )
        return 1

    print("@Preview 본문에 네트워크 URL 없음")
    return 0


if __name__ == "__main__":
    sys.exit(main())
