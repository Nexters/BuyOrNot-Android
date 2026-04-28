package com.sseotdabwa.buyornot.core.data.repository

import com.sseotdabwa.buyornot.core.network.api.NotificationApiService
import com.sseotdabwa.buyornot.core.network.dto.response.NotificationResponse
import com.sseotdabwa.buyornot.core.network.dto.response.getOrThrow
import com.sseotdabwa.buyornot.domain.model.Notification
import com.sseotdabwa.buyornot.domain.model.NotificationType
import com.sseotdabwa.buyornot.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApiService: NotificationApiService,
) : NotificationRepository {
    override suspend fun getNotifications(type: String?): List<Notification> =
        notificationApiService.getNotifications(type).getOrThrow().map { it.toDomain() }

    override suspend fun markAsRead(notificationId: Long) {
        notificationApiService.markAsRead(notificationId).getOrThrow()
    }

    private fun NotificationResponse.toDomain(): Notification =
        Notification(
            notificationId = notificationId,
            feedId = feedId,
            type = NotificationType.fromString(type),
            title = title,
            body = body,
            isRead = isRead,
            voteClosedAt = voteClosedAt,
            resultPercent = resultPercent,
            resultLabel = resultLabel,
            viewUrl = viewUrl,
            feedTitle = feedTitle.orEmpty(),
        )
}
