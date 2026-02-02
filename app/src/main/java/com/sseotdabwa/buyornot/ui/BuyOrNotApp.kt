package com.sseotdabwa.buyornot.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.sseotdabwa.buyornot.core.designsystem.components.ExpandableFloatingActionButton
import com.sseotdabwa.buyornot.core.designsystem.components.FabOption
import com.sseotdabwa.buyornot.core.designsystem.icon.BuyOrNotIcons
import com.sseotdabwa.buyornot.core.designsystem.icon.asImageVector
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.feature.upload.navigation.UPLOAD_ROUTE
import com.sseotdabwa.buyornot.navigation.BuyOrNotNavHost

@Composable
fun BuyOrNotApp() {
    val navController = rememberNavController()
    var isFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExpandableFloatingActionButton(
                expanded = isFabExpanded,
                onExpandedChange = { isFabExpanded = it },
                options =
                    listOf(
                        FabOption(
                            icon = BuyOrNotIcons.Bag.asImageVector(),
                            label = "업로드",
                            onClick = {
                                navController.navigate(UPLOAD_ROUTE)
                            },
                        ),
                    ),
            )
        },
        containerColor = BuyOrNotTheme.colors.gray0,
    ) { innerPadding ->
        BuyOrNotNavHost(
            navController = navController,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
        )
    }
}
