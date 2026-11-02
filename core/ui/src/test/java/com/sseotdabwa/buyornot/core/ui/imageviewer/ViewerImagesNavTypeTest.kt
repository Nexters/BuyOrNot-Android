package com.sseotdabwa.buyornot.core.ui.imageviewer

import android.app.Application
import android.net.Uri
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import com.sseotdabwa.buyornot.core.designsystem.image.ImageModel
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Serializable
internal object ViewerTestStartRoute

// 기본 SDK(compileSdk)는 Java 21 샌드박스를 요구해 Java 17 툴체인에서 뜨지 않는다.
// 모듈 robolectric.properties의 스크린샷용 Application 대신 기본 Application으로 띄운다.
@Config(sdk = [34], application = Application::class)
@RunWith(RobolectricTestRunner::class)
class ViewerImagesNavTypeTest {
    private val navController =
        TestNavHostController(RuntimeEnvironment.getApplication()).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
            graph =
                createGraph(startDestination = ViewerTestStartRoute) {
                    composable<ViewerTestStartRoute> {}
                    imageViewerScreen(onBackClick = {})
                }
        }

    @Test
    fun `이미지_뷰어로_이동하면_라우트에서_종류와_값이_그대로_복원된다`() {
        val images =
            listOf(
                ImageModel.Network("https://cdn.example.com/a b.jpg?w=1&h=2"),
                ImageModel.Network("https://cdn.example.com/dir/a%20b{1}.jpg"),
                ImageModel.Resource(42),
                ImageModel.Local(Uri.parse("content://media/external/images/7")),
            )

        navController.navigateToImageViewer(images, initialPage = 2)

        val route = navController.currentBackStackEntry!!.toRoute<ImageViewerRoute>()
        assertEquals(images, route.images.map { it.toImageModel() })
        assertEquals(2, route.initialPage)
    }
}
