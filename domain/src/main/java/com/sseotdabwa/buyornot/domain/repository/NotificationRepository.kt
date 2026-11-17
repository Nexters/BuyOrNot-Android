package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(): List<Notification>

    suspend fun markAsRead(notificationId: Long)

    suspend fun getUnreadCount(): Int
}
