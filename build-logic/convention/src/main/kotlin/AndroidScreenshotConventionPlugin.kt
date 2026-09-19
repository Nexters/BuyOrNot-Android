import com.android.build.api.dsl.LibraryExtension
import com.github.takahirom.roborazzi.AnnotationFilter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.sseotdabwa.convention.libs
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * 스크린샷 비교 테스트를 켠다 (#153).
 *
 * Roborazzi가 모듈의 `@Preview` 컴포저블을 스캔해 Robolectric 테스트를 생성하고,
 * `recordRoborazziDebug` / `compareRoborazziDebug` 로 골든을 기록·비교한다.
 */
class AndroidScreenshotConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.github.takahirom.roborazzi")

            // 스캔 범위는 모듈 경로에서 유도한다 — android namespace 와 같은 규칙이라
            // 모듈이 늘어도 플러그인을 고칠 일이 없다. (:core:designsystem -> ...core.designsystem)
            val screenshotPackage = "com.sseotdabwa.buyornot" + path.replace(":", ".")

            extensions.configure<RoborazziExtension> {
                generateComposePreviewRobolectricTests {
                    enable.set(true)
                    packages.set(listOf(screenshotPackage))
                    // 이 레포의 프리뷰는 전부 private fun 이다.
                    includePrivatePreviews.set(true)
                    @OptIn(ExperimentalRoborazziApi::class)
                    annotationFilter.set(
                        AnnotationFilter.Exclude(SCREENSHOT_TEST_EXCLUDE_ANNOTATION),
                    )
                }
            }

            extensions.configure<LibraryExtension> {
                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                        // Roborazzi 권고. 이게 없으면 하드웨어 가속 렌더 경로를 타지 못해
                        // 렌더 결과가 달라진다.
                        all {
                            it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
                        }
                    }
                }
            }

            dependencies {
                add("testImplementation", project(":core:testing"))
                // Roborazzi 플러그인은 아래 의존성들이 모듈에 '직접' 선언됐는지 검사한다.
                // core:testing 을 통한 전이 의존성으로는 통과하지 못하므로 여기서 같이 건다.
                add("testImplementation", libs.findLibrary("roborazzi-compose-preview-scanner-support").get())
                add("testImplementation", libs.findLibrary("composable-preview-scanner-android").get())
                add("testImplementation", libs.findLibrary("robolectric").get())
                add("testImplementation", libs.findLibrary("junit").get())
            }
        }
    }

    private companion object {
        const val SCREENSHOT_TEST_EXCLUDE_ANNOTATION =
            "com.sseotdabwa.buyornot.core.designsystem.preview.ScreenshotTestExclude"
    }
}
