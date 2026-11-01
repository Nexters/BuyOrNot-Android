package com.sseotdabwa.buyornot.core.designsystem.components

private const val MAX_DISPLAY_COUNT = 99

/** 개수를 화면에 보여줄 때 상한을 넘으면 "99+"처럼 줄인다. */
fun Int.toDisplayCount(): String = if (this > MAX_DISPLAY_COUNT) "$MAX_DISPLAY_COUNT+" else toString()
