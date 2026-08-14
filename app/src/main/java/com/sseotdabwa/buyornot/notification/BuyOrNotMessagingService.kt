package com.sseotdabwa.buyornot.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sseotdabwa.buyornot.BuildConfig
import com.sseotdabwa.buyornot.MainActivity
import com.sseotdabwa.buyornot.R
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "FCM"
private const val CHANNEL_ID = "buyornot_default_channel"
private const val CHANNEL_NAME = "살까말까 알림"
private const val DEFAULT_TITLE = "살까말까"
private const val DEFAULT_BODY = "새로운 소식이 도착했어요."

// feedId/notificationId가 모두 없는 마케팅 알림용 고정 notify id.
// 마케팅 알림끼리는 최신 것으로 덮어써도 무방하므로 단일 상수를 사용한다.
private const val MARKETING_NOTIFICATION_ID = 0

@AndroidEntryPoint
class BuyOrNotMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onNewToken - FCM token: $token")
        }
        // TODO: Send token to server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onMessageReceived - from: ${message.from}")
            Log.d(TAG, "onMessageReceived - data payload: ${message.data}")
            Log.d(
                TAG,
                "onMessageReceived - notification: title=${message.notification?.title}, " +
                    "body=${message.notification?.body}",
            )
        }

        val feedId = message.data[FcmKeys.FEED_ID]?.toLongOrNull()
        val notificationId = message.data[FcmKeys.NOTIFICATION_ID]?.toLongOrNull()
        val type = message.data[FcmKeys.TYPE]
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onMessageReceived - type=$type, feedId=$feedId, notificationId=$notificationId")
        }

        showFeedNotification(
            type = type,
            feedId = feedId,
            notificationId = notificationId,
            title = message.notification?.title ?: DEFAULT_TITLE,
            body = message.notification?.body ?: DEFAULT_BODY,
        )
    }

    private fun showFeedNotification(
        type: String?,
        feedId: Long?,
        notificationId: Long?,
        title: String,
        body: String,
    ) {
        createNotificationChannel()

        // 딥링크용 feedId/notificationId는 있을 때만 심는다(마케팅 알림은 없음).
        // type은 유입 로깅(push_opened)의 탭 감지 마커라 마케팅 알림에도 항상 심어, 이 포그라운드
        // 경로가 FCM이 만드는 백그라운드 launch Intent와 같은 extra 구성을 갖도록 맞춘다.
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(FcmKeys.TYPE, type ?: FcmKeys.UNKNOWN_TYPE)
                if (feedId != null) putExtra(FcmKeys.FEED_ID, feedId.toString())
                if (notificationId != null) putExtra(FcmKeys.NOTIFICATION_ID, notificationId.toString())
            }
        // notify id / requestCode: notificationId > feedId > 마케팅 고정 상수 순으로 안정적인 id를 사용한다.
        val systemNotificationId = (notificationId ?: feedId)?.hashCode() ?: MARKETING_NOTIFICATION_ID
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                systemNotificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "showFeedNotification - POST_NOTIFICATIONS not granted, skip posting")
            return
        }

        NotificationManagerCompat.from(this).notify(systemNotificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH,
                )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
