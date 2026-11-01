package com.sseotdabwa.buyornot.core.designsystem.components

private const val MAX_DISPLAY_COUNT = 99

/** 개수가 화면에 보여줄 상한을 넘으면 상한 뒤에 "+"를 붙여 줄인다. */
fun Int.toDisplayCount(): String = if (this > MAX_DISPLAY_COUNT) "$MAX_DISPLAY_COUNT+" else toString()
