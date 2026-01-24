package com.sseotdabwa.buyornot.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(
    name = "Color Catalog",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun ColorCatalogPreview() {
    BuyOrNotTheme {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(
                text = "BuyOrNot Color Catalog",
                style = BuyOrNotTheme.typography.displayD1Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Black & Gray Colors
            Text(
                text = "Black & Gray",
                style = BuyOrNotTheme.typography.headingH2Bold,
                color = BuyOrNotTheme.colors.gray900,
            )
            Spacer(modifier = Modifier.height(16.dp))

            ColorItem("black", BuyOrNotTheme.colors.black, "#000000")
            ColorItem("gray1000", BuyOrNotTheme.colors.gray1000, "#1A1C20")
            ColorItem("gray900", BuyOrNotTheme.colors.gray900, "#2A3038")
            ColorItem("gray800", BuyOrNotTheme.colors.gray800, "#565D6D")
            ColorItem("gray700", BuyOrNotTheme.colors.gray700, "#868B94")
            ColorItem("gray600", BuyOrNotTheme.colors.gray600, "#B1B3BB")
            ColorItem("gray500", BuyOrNotTheme.colors.gray500, "#D2D3D9")
            ColorItem("gray400", BuyOrNotTheme.colors.gray400, "#DDDEE4")
            ColorItem("gray300", BuyOrNotTheme.colors.gray300, "#EEEFF1")
            ColorItem("gray200", BuyOrNotTheme.colors.gray200, "#F3F4F5")
            ColorItem("gray100", BuyOrNotTheme.colors.gray100, "#F7F8F9")
            ColorItem("gray50", BuyOrNotTheme.colors.gray50, "#FBFBFC")
            ColorItem("gray0", BuyOrNotTheme.colors.gray0, "#FFFFFF")

            Spacer(modifier = Modifier.height(24.dp))

            // Chromatic Colors
            Text(
                text = "Chromatic",
                style = BuyOrNotTheme.typography.headingH2Bold,
                color = BuyOrNotTheme.colors.gray900,
            )
            Spacer(modifier = Modifier.height(16.dp))

            ColorItem("green200", BuyOrNotTheme.colors.green200, "#0DAC7D")
            ColorItem("green100", BuyOrNotTheme.colors.green100, "#42C694")
            ColorItem("red100", BuyOrNotTheme.colors.red100, "#FF3830")
            ColorItem("blue100", BuyOrNotTheme.colors.blue100, "#217CF9")
        }
    }
}

@Composable
private fun ColorItem(
    name: String,
    color: Color,
    hexCode: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(60.dp)
                    .background(color = color, shape = RoundedCornerShape(8.dp)),
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = name,
                style = BuyOrNotTheme.typography.titleT2Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )
            Text(
                text = hexCode,
                style = BuyOrNotTheme.typography.bodyB4Medium,
                color = BuyOrNotTheme.colors.gray700,
            )
        }
    }
}

@Preview(
    name = "Typography Catalog",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun TypographyCatalogPreview() {
    BuyOrNotTheme {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(
                text = "BuyOrNot Typography Catalog",
                style = BuyOrNotTheme.typography.displayD1Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Display
            TypographySection("Display") {
                TypographyItem("displayD1Bold", BuyOrNotTheme.typography.displayD1Bold)
                TypographyItem("displayD2Bold", BuyOrNotTheme.typography.displayD2Bold)
            }

            // Heading
            TypographySection("Heading") {
                TypographyItem("headingH1Bold", BuyOrNotTheme.typography.headingH1Bold)
                TypographyItem("headingH2Bold", BuyOrNotTheme.typography.headingH2Bold)
                TypographyItem("headingH3Bold", BuyOrNotTheme.typography.headingH3Bold)
                TypographyItem("headingH4Bold", BuyOrNotTheme.typography.headingH4Bold)
                TypographyItem("headingH1SemiBold", BuyOrNotTheme.typography.headingH1SemiBold)
            }

            // Title
            TypographySection("Title") {
                TypographyItem("titleT1Bold", BuyOrNotTheme.typography.titleT1Bold)
                TypographyItem("titleT2Bold", BuyOrNotTheme.typography.titleT2Bold)
                TypographyItem("titleT3Bold", BuyOrNotTheme.typography.titleT3Bold)
                TypographyItem("titleT4Bold", BuyOrNotTheme.typography.titleT4Bold)
            }

            // SubTitle
            TypographySection("SubTitle") {
                TypographyItem("subTitleS1SemiBold", BuyOrNotTheme.typography.subTitleS1SemiBold)
                TypographyItem("subTitleS2SemiBold", BuyOrNotTheme.typography.subTitleS2SemiBold)
                TypographyItem("subTitleS3SemiBold", BuyOrNotTheme.typography.subTitleS3SemiBold)
                TypographyItem("subTitleS4SemiBold", BuyOrNotTheme.typography.subTitleS4SemiBold)
                TypographyItem("subTitleS5SemiBold", BuyOrNotTheme.typography.subTitleS5SemiBold)
            }

            // Body
            TypographySection("Body") {
                TypographyItem("bodyB1Medium", BuyOrNotTheme.typography.bodyB1Medium)
                TypographyItem("bodyB2Medium", BuyOrNotTheme.typography.bodyB2Medium)
                TypographyItem("bodyB3Medium", BuyOrNotTheme.typography.bodyB3Medium)
                TypographyItem("bodyB4Medium", BuyOrNotTheme.typography.bodyB4Medium)
                TypographyItem("bodyB5Medium", BuyOrNotTheme.typography.bodyB5Medium)
                TypographyItem("bodyB6Medium", BuyOrNotTheme.typography.bodyB6Medium)
                TypographyItem("bodyB7Medium", BuyOrNotTheme.typography.bodyB7Medium)
            }

            // Caption
            TypographySection("Caption") {
                TypographyItem("captionC1Medium", BuyOrNotTheme.typography.captionC1Medium)
                TypographyItem("captionC2Medium", BuyOrNotTheme.typography.captionC2Medium)
                TypographyItem("captionC3Medium", BuyOrNotTheme.typography.captionC3Medium)
                TypographyItem("captionC1Regular", BuyOrNotTheme.typography.captionC1Regular)
                TypographyItem("captionC2Regular", BuyOrNotTheme.typography.captionC2Regular)
                TypographyItem("captionC3Regular", BuyOrNotTheme.typography.captionC3Regular)
            }
        }
    }
}

@Composable
private fun TypographySection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = BuyOrNotTheme.typography.headingH2Bold,
            color = BuyOrNotTheme.colors.gray900,
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TypographyItem(
    name: String,
    textStyle: TextStyle,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = name,
            style = BuyOrNotTheme.typography.bodyB5Medium,
            color = BuyOrNotTheme.colors.gray700,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "The quick brown fox jumps over the lazy dog",
            style = textStyle,
            color = BuyOrNotTheme.colors.gray1000,
        )
        Text(
            text = "Size: ${textStyle.fontSize}, LineHeight: ${textStyle.lineHeight}, Weight: ${textStyle.fontWeight?.weight}",
            style = BuyOrNotTheme.typography.captionC2Regular,
            color = BuyOrNotTheme.colors.gray600,
        )
    }
}

@Preview(
    name = "Combined Catalog",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
)
@Composable
private fun CombinedCatalogPreview() {
    BuyOrNotTheme {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Text(
                text = "BuyOrNot Design System",
                style = BuyOrNotTheme.typography.displayD1Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Complete color and typography showcase",
                style = BuyOrNotTheme.typography.bodyB2Medium,
                color = BuyOrNotTheme.colors.gray700,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Color Samples
            Text(
                text = "Quick Color Samples",
                style = BuyOrNotTheme.typography.headingH2Bold,
                color = BuyOrNotTheme.colors.gray900,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ColorSwatch("green200", BuyOrNotTheme.colors.green200)
                ColorSwatch("green100", BuyOrNotTheme.colors.green100)
                ColorSwatch("red100", BuyOrNotTheme.colors.red100)
                ColorSwatch("blue100", BuyOrNotTheme.colors.blue100)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Typography Samples
            Text(
                text = "Quick Typography Samples",
                style = BuyOrNotTheme.typography.headingH2Bold,
                color = BuyOrNotTheme.colors.gray900,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Display D1 Bold - The quick brown fox",
                style = BuyOrNotTheme.typography.displayD1Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Heading H1 Bold - The quick brown fox jumps",
                style = BuyOrNotTheme.typography.headingH1Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Title T1 Bold - The quick brown fox jumps over the lazy dog",
                style = BuyOrNotTheme.typography.titleT1Bold,
                color = BuyOrNotTheme.colors.gray1000,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SubTitle S2 SemiBold - The quick brown fox jumps over the lazy dog",
                style = BuyOrNotTheme.typography.subTitleS2SemiBold,
                color = BuyOrNotTheme.colors.gray800,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Body B2 Medium - The quick brown fox jumps over the lazy dog. This is a sample text to showcase body typography.",
                style = BuyOrNotTheme.typography.bodyB2Medium,
                color = BuyOrNotTheme.colors.gray800,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Caption C1 Regular - The quick brown fox jumps over the lazy dog. This is caption text.",
                style = BuyOrNotTheme.typography.captionC1Regular,
                color = BuyOrNotTheme.colors.gray700,
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(70.dp)
                    .background(color = color, shape = RoundedCornerShape(12.dp)),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = BuyOrNotTheme.typography.captionC2Medium,
            color = BuyOrNotTheme.colors.gray800,
        )
    }
}
