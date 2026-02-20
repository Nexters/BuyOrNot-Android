package com.sseotdabwa.buyornot.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BuyOrNotMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        // TODO: Send token to server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "From: ${message.from}")

        // Check if message contains a notification payload.
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }

        // Check if message contains a data payload.
        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${message.data}")
        }
    }
}
