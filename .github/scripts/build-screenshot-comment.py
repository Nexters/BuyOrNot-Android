#!/usr/bin/env python3
"""스크린샷 diff 리포트 PR 코멘트 본문을 만든다 (#153).

Roborazzi는 렌더가 골든과 다를 때만 `*_compare.png` 를 남긴다. 따라서 그 목록이 비어
있다는 것은 이 PR이 화면을 아무것도 바꾸지 않았다는 뜻이다.

여기서 다루는 파일 목록은 PR이 올린 코드가 만들어낸 산출물이므로 신뢰할 수 없는 입력으로
취급한다. roborazzi 출력 경로 밖이거나 경로 이스케이프가 섞인 것은 커밋도 링크도 하지 않는다.

사용법:
    build-screenshot-comment.py <diff-root> <repo> <branch> <out-comment> <out-filelist>
"""

from __future__ import annotations

import sys
from pathlib import Path, PurePosixPath
from urllib.parse import quote

# 코멘트 본문은 65536자를 넘으면 API가 거부한다. 스크린샷 id가 긴 편이라 여유를 둔다.
MAX_ROWS = 50
# 이름이 길면 표 칸을 넘치므로 이 길이마다 줄을 바꾼다.
WRAP_EVERY = 28
# 모든 스크린샷 id가 이 접두사로 시작한다. 표에서는 잡음이라 떼어낸다.
ID_PREFIX = "com.sseotdabwa.buyornot."
ID_SUFFIX = "_compare.png"


def collect(diff_root: Path) -> list[PurePosixPath]:
    found: list[PurePosixPath] = []
    for path in diff_root.rglob("*_compare.png"):
        rel = PurePosixPath(path.relative_to(diff_root).as_posix())
        # roborazzi가 쓴 자리에서 나온 것만 받는다.
        if "build/outputs/roborazzi" not in str(rel):
            continue
        # 경로 이스케이프는 거부한다. 심볼릭 링크도 따라가지 않는다.
        if ".." in rel.parts or path.is_symlink():
            continue
        # 개행이 든 경로는 로보라찌가 만들 수 없다. 프리뷰 이름(@Preview(name = ...))이
        # 스크린샷 id 에 들어가므로 여기로 개행을 흘려보낼 수 있고, 그러면 git 이 한 경로를
        # 여러 pathspec 으로 쪼개 뒤쪽 줄이 pathspec magic 으로 해석된다.
        if "\n" in str(rel) or "\r" in str(rel) or "\0" in str(rel):
            continue
        found.append(rel)
    # rglob 순서는 파일시스템에 따라 달라진다. 잘린 리포트가 실행마다 다른 행을 남기지
    # 않도록 정렬한다.
    return sorted(found)


def display_name(file_name: str) -> str:
    """표에 쓸 짧은 이름. 스크린샷 id는 FQN 전체라 그대로 두면 표를 못 읽는다."""
    name = file_name.removeprefix(ID_PREFIX).removesuffix(ID_SUFFIX)
    return "<br>".join(name[i : i + WRAP_EVERY] for i in range(0, len(name), WRAP_EVERY))


def module_of(rel: PurePosixPath) -> str:
    """경로 앞부분이 모듈이다. core/designsystem/build/outputs/... -> core:designsystem"""
    parts = str(rel).split("/build/outputs/")[0].split("/")
    return ":".join(parts)


def build_comment(files: list[PurePosixPath], repo: str, branch: str) -> str:
    lines = ["스크린샷 비교 리포트", ""]

    if not files:
        lines.append("이 PR은 프리뷰 렌더를 바꾸지 않았습니다.")
        return "\n".join(lines) + "\n"

    lines.append(f"프리뷰 **{len(files)}개**의 렌더가 base 와 다릅니다. 의도한 변경인지 확인해주세요.")
    lines.append("")
    lines.append("| 모듈 | 프리뷰 | Reference / Diff / New |")
    lines.append("|---|---|---|")

    tree = f"https://github.com/{repo}/tree/{quote(branch)}"
    for rel in files[:MAX_ROWS]:
        # 스크린샷 id에 한글이 섞이므로 URL은 퍼센트 인코딩한다.
        url = f"https://github.com/{repo}/blob/{quote(branch)}/{quote(str(rel))}"
        lines.append(
            f"| `{module_of(rel)}` | [{display_name(rel.name)}]({url}) | ![]({url}?raw=true) |"
        )

    if len(files) > MAX_ROWS:
        lines.append("")
        lines.append(
            f"{len(files)}개 중 {MAX_ROWS}개만 표시했습니다. "
            f"전체는 [컴패니언 브랜치]({tree})에 있습니다."
        )

    return "\n".join(lines) + "\n"


def main() -> int:
    if len(sys.argv) != 6:
        print(__doc__, file=sys.stderr)
        return 2

    diff_root, repo, branch, out_comment, out_filelist = (
        Path(sys.argv[1]),
        sys.argv[2],
        sys.argv[3],
        Path(sys.argv[4]),
        Path(sys.argv[5]),
    )

    files = collect(diff_root) if diff_root.is_dir() else []
    # git add --pathspec-file-nul 이 읽는 형식. 개행 구분이면 한 경로가 쪼개질 수 있다.
    out_filelist.write_bytes(b"".join(f"{f}\0".encode() for f in files))
    out_comment.write_text(build_comment(files, repo, branch), encoding="utf-8")

    print(f"diff {len(files)}건")
    return 0


if __name__ == "__main__":
    sys.exit(main())
