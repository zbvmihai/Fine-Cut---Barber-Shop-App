package com.finecut.barbershop.utils

import android.content.ContentValues.TAG
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.finecut.barbershop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

// This is the my firebase messaging service class that setup the firebase messaging service
// when a notification is send via FCM service.
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle FCM messages here.
        if (remoteMessage.notification != null) {
            showNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationId = Random.nextInt()
        val channelId = "booking_status_channel"

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.finecut_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(notificationId, builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Get the user ID of the currently signed-in user.
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Update the FCM token in the user's database node.
            val database = FirebaseDatabase.getInstance().reference
            database.child("Users").child(userId).child("fcmToken").setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token updated for user $userId")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Error updating FCM token for user $userId", it)
                }
        }
    }
}