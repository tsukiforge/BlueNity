package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.QuickCleanReceiver

object QuickCleanNotificationHelper {

    const val CHANNEL_ID = "game_prep_quick_clean_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pembersih Cepat Game Prep"
            val descriptionText = "Pintasan pembersihan cache dan RAM di Action Center / Notif"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows actionable prompt in notification center.
     * When user taps this notification or the action button, it triggers the clean action.
     * Not ongoing (can be dismissed by user).
     */
    fun showPromptNotification(context: Context) {
        createNotificationChannel(context)

        val cleanIntent = Intent(context, QuickCleanReceiver::class.java).apply {
            action = QuickCleanReceiver.ACTION_QUICK_CLEAN
        }
        val cleanPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            cleanIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_clean)
            .setContentTitle("Game Prep: Hapus Cache & RAM")
            .setContentText("Ketuk di sini untuk bersihkan cache & lihat hasilnya")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Ketuk notifikasi ini untuk membersihkan cache & menghentikan proses latar belakang. Hasil berapa MB yang dihapus akan langsung muncul.")
            )
            .setContentIntent(cleanPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_notification_clean,
                "Hapus Sekarang",
                cleanPendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ permission not granted yet
        } catch (_: Exception) {}
    }

    /**
     * Shows the result notification showing exactly how many MB / KB were cleaned.
     * Triggered immediately after the user taps "Hapus Cache" in Action Center or Notif.
     */
    fun showCleanResultNotification(
        context: Context,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cleanIntent = Intent(context, QuickCleanReceiver::class.java).apply {
            action = QuickCleanReceiver.ACTION_QUICK_CLEAN
        }
        val cleanPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            cleanIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_clean)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setSilent(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(
                R.drawable.ic_notification_clean,
                "Hapus Lagi",
                cleanPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "Buka Game Prep",
                openAppPendingIntent
            )
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
        } catch (_: Exception) {}
    }

    fun showNotification(
        context: Context,
        statusTitle: String? = null,
        statusSubtitle: String? = null
    ) {
        if (statusTitle != null && statusSubtitle != null) {
            showCleanResultNotification(context, statusTitle, statusSubtitle)
        } else {
            showPromptNotification(context)
        }
    }

    fun cancelNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }
}
