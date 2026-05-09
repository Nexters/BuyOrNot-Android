package com.sseotdabwa.buyornot.feature.auth.ui

import com.sseotdabwa.buyornot.domain.model.AppUpdateInfo
import com.sseotdabwa.buyornot.domain.model.UpdateStrategy
import org.junit.Test
import kotlin.test.assertEquals

class SplashUpdateLogicTest {
    private val now = 1_000_000_000L

    @Test
    fun updateInfo가_null이면_None을_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 10,
                updateInfo = null,
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.None, result)
    }

    @Test
    fun currentVersion이_minimumVersion_미만이면_Force를_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 5,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 6,
                        updateStrategy = UpdateStrategy.NONE,
                    ),
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.Force, result)
    }

    @Test
    fun FORCE_전략이고_currentVersion이_latestVersion_미만이면_Force를_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 8,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.FORCE,
                    ),
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.Force, result)
    }

    @Test
    fun FORCE_전략이지만_currentVersion이_latestVersion_이상이면_None을_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 10,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.FORCE,
                    ),
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.None, result)
    }

    @Test
    fun FORCE_전략이고_minimumVersion_이상_latestVersion_미만이면_Force를_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 7,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.FORCE,
                    ),
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.Force, result)
    }

    @Test
    fun SOFT_전략이고_24시간_이상_지났으면_Soft를_반환한다() {
        val lastShown = now - SOFT_UPDATE_INTERVAL_MILLIS
        val result =
            resolveUpdateDialogType(
                currentVersion = 8,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.SOFT,
                    ),
                lastSoftUpdateShownTime = lastShown,
                now = now,
            )
        assertEquals(UpdateDialogType.Soft, result)
    }

    @Test
    fun SOFT_전략이지만_24시간_미만이면_None을_반환한다() {
        val lastShown = now - SOFT_UPDATE_INTERVAL_MILLIS + 1
        val result =
            resolveUpdateDialogType(
                currentVersion = 8,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.SOFT,
                    ),
                lastSoftUpdateShownTime = lastShown,
                now = now,
            )
        assertEquals(UpdateDialogType.None, result)
    }

    @Test
    fun SOFT_전략이지만_currentVersion이_latestVersion_이상이면_None을_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 10,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.SOFT,
                    ),
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.None, result)
    }

    @Test
    fun 시계가_역행했을때_lastShown이_무효화되어_Soft를_반환한다() {
        // lastSoftUpdateShownTime이 now보다 미래인 경우 (시계 역행) → effectiveLastShown = 0 → 항상 Soft
        val lastShownInFuture = now + 1_000L
        val result =
            resolveUpdateDialogType(
                currentVersion = 8,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.SOFT,
                    ),
                lastSoftUpdateShownTime = lastShownInFuture,
                now = now,
            )
        assertEquals(UpdateDialogType.Soft, result)
    }

    @Test
    fun NONE_전략이면_None을_반환한다() {
        val result =
            resolveUpdateDialogType(
                currentVersion = 8,
                updateInfo =
                    AppUpdateInfo(
                        latestVersion = 10,
                        minimumVersion = 5,
                        updateStrategy = UpdateStrategy.NONE,
                    ),
                lastSoftUpdateShownTime = 0L,
                now = now,
            )
        assertEquals(UpdateDialogType.None, result)
    }

    @Test
    fun minimumVersion_미달이면_전략_무관하게_Force를_반환한다() {
        for (strategy in UpdateStrategy.entries) {
            val result =
                resolveUpdateDialogType(
                    currentVersion = 3,
                    updateInfo =
                        AppUpdateInfo(
                            latestVersion = 10,
                            minimumVersion = 5,
                            updateStrategy = strategy,
                        ),
                    lastSoftUpdateShownTime = 0L,
                    now = now,
                )
            assertEquals(
                UpdateDialogType.Force,
                result,
                "strategy=$strategy 일 때 minimumVersion 미달은 Force여야 함",
            )
        }
    }
}
