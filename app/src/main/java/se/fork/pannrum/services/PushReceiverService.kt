package se.fork.pannrum.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.CaseMap
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import se.fork.pannrum.MainActivity
import se.fork.pannrum.R
import timber.log.Timber

class PushReceiverService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Timber.d("onNewToken: $token")
        super.onNewToken(token)
        registerDeviceToken(token)
    }

    private fun registerDeviceToken(token: String?) {
        Timber.d("registerDeviceToken $token")
        token?.let {
            // TODO
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Timber.d("onMessageReceived: $message")

        var title = message.notification?.title ?: ""
        var body = message.notification?.body ?: ""
        var id = ""

        Timber.d("onMessageReceived title = $title, body = $body")
        val data = message.data

        val extras = mutableMapOf<String, String>()

        for (entry in data.entries) {
            Timber.d("onMessageReceived (data entry): ${entry.key} = ${entry.value}")
            when (entry.key) {
                "title" -> title = entry.value
                "body" -> body = entry.value
                else -> extras.put(entry.key, entry.value)
            }
        }

        Timber.d("onMessageReceived: Ready to make notification: $title $body")

        sendLocalNotification(title, body, extras)
    }

    private fun sendLocalNotification(title: String, body: String, extras: MutableMap<String, String>) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("Cancerfonden", "Cancerfonden", importance)
            channel.description = body
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "Cancerfonden")
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setColor(Color.parseColor("#FFD600"))
                .setContentIntent(pendingIntent)
                .setChannelId("Pannrum")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        notificationManager.notify(0, notification)
    }
}