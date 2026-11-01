import unittest

from check_comments import COMMIT_PATTERN, PR_CREATE_PATTERN, changed_comments, comment_text

DIFF = """\
diff --git a/feature/a/Foo.kt b/feature/a/Foo.kt
--- a/feature/a/Foo.kt
+++ b/feature/a/Foo.kt
@@ -10,0 +11,4 @@ class Foo {
+    // 서버가 같은 요청을 거절하므로 재시도하지 않는다.
+    val url = "https://example.com"
+    val x = 1 // 기존에는 2였는데 리뷰 반영으로 바꿨다.
+    /** 간격 8dp */
@@ -20 +25 @@ class Foo {
-    // 지운 주석
+    val y = 2
diff --git a/README.md b/README.md
--- a/README.md
+++ b/README.md
@@ -1,0 +2 @@
+// 문서는 검사하지 않는다
"""


class CommandPatternTest(unittest.TestCase):
    def test_커밋과_PR_생성_명령만_잡는다(self):
        self.assertTrue(COMMIT_PATTERN.search("git commit -m 'x'"))
        self.assertTrue(COMMIT_PATTERN.search("git add . && git -c commit.gpgsign=false commit -q -m x"))
        self.assertTrue(PR_CREATE_PATTERN.search("gh pr create --base develop"))
        self.assertFalse(COMMIT_PATTERN.search("git log --grep commit"))
        self.assertFalse(COMMIT_PATTERN.search("git status"))
        self.assertFalse(PR_CREATE_PATTERN.search("gh pr view 1"))


class CommentExtractionTest(unittest.TestCase):
    def test_URL은_주석으로_보지_않는다(self):
        self.assertIsNone(comment_text('    val url = "https://example.com"'))

    def test_코드_뒤에_붙은_주석도_꺼낸다(self):
        self.assertEqual("// 이유", comment_text("    val x = 1 // 이유"))

    def test_추가된_소스_주석만_새_줄_번호와_함께_모은다(self):
        comments = changed_comments(DIFF)

        self.assertEqual(
            [("feature/a/Foo.kt", 11), ("feature/a/Foo.kt", 13), ("feature/a/Foo.kt", 14)],
            [(c.path, c.line) for c in comments],
        )

    def test_경위와_수치_표현을_표시한다(self):
        comments = changed_comments(DIFF)

        self.assertEqual([], comments[0].hints)
        self.assertEqual(["경위"], comments[1].hints)
        self.assertEqual(["수치 재기재"], comments[2].hints)


if __name__ == "__main__":
    unittest.main()
