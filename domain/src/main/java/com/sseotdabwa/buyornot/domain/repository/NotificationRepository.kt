package com.sseotdabwa.buyornot.domain.repository

import com.sseotdabwa.buyornot.domain.model.Notification

interface NotificationRepository {
    suspend fun getNotifications(type: String?): List<Notification>

    suspend fun markAsRead(notificationId: Long)
}
