package com.finecut.barbershop.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.finecut.barbershop.R

// This is the Follow Up Reminder Broadcast class that setup the the
// notification channel, title, description and icon of the follow up reminder notification
class FollowUpReminderBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "reminders_channel"
        val notificationId = 1002 // Unique integer for the follow-up notification

        val title = intent.getStringExtra("title") ?: "We Miss You!"
        val message = intent.getStringExtra("message") ?: "It's been 2 weeks since your last appointment. Would you like to schedule a new one?"

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.finecut_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel if running on Android Oreo or later
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, "Follow-Up Reminder", importance).apply {
                description = "A reminder to schedule a new appointment after 2 weeks"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Display the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}