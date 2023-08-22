package io.github.duzhaokun123.yarc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.preference.PreferenceManager
import android.widget.RemoteViews
import android.widget.Toast
import com.topjohnwu.superuser.ipc.RootService

class ForegroundService : Service() {
    companion object {
        val IDS = mapOf(
            ActivityInfo.SCREEN_ORIENTATION_SENSOR to Triple(R.id.btn_sensor, "S", "Sensor"),
            ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR to Triple(R.id.btn_full_sensor, "FS", "Full Sensor"),
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE to Triple(R.id.btn_landscape, "L", "Landscape"),
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE to Triple(R.id.btn_reverse_landscape, "RL", "Reverse Landscape"),
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE to Triple(R.id.btn_sensor_landscape, "SL", "Sensor Landscape"),
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT to Triple(R.id.btn_portrait, "P", "Portrait"),
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT to Triple(R.id.btn_reverse_portrait, "RP", "Reverse Portrait"),
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT to Triple(R.id.btn_sensor_portrait, "SP", "Sensor Portrait"),
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED to Triple(R.id.btn_unspecified, "U", "Unspecified"),
        )

        const val ACTION_UPDATE = "update"
        const val EXTRA_ORIENTATION = "orientation"
    }

    val rootConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            messenger = Messenger(service)
            startService(Intent(this@ForegroundService, ForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_ORIENTATION, messenger)
            })
        }

        override fun onServiceDisconnected(name: ComponentName) {
            messenger = null
            Toast.makeText(
                this@ForegroundService,
                "[YARC] root service disconnected",
                Toast.LENGTH_SHORT
            ).show()
            stopSelf()
        }
    }
    var messenger: Messenger? = null
    var orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE -> {
                orientation = intent.getIntExtra(
                    EXTRA_ORIENTATION, preferences.getInt("orientation", orientation)
                )
                preferences.edit().putInt("orientation", orientation).apply()
                messenger?.send(Message.obtain(null, WindowService.MSG_UPDATE, orientation, 2))
                    ?: Toast.makeText(this, "[YARC] root service not connected", Toast.LENGTH_SHORT)
                        .show()
            }
        }
        startForeground(1, createNotification())
        if (messenger == null) {
            RootService.bind(Intent(this, WindowService::class.java), rootConnection)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(): Notification {
        val channel =
            NotificationChannel("rc", "Rotation Control", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val view = RemoteViews(BuildConfig.APPLICATION_ID, R.layout.notification)
        IDS.forEach { (orientation, v) ->
            val (id) = v
            view.setOnClickPendingIntent(
                id,
                PendingIntent.getService(
                    this,
                    id,
                    Intent(this, ForegroundService::class.java).apply {
                        action = ACTION_UPDATE
                        putExtra(EXTRA_ORIENTATION, orientation)
                    },
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        val sort = if (messenger == null) "[!]" else IDS[orientation]?.second ?: "($orientation)"
        val long = if (messenger == null) "[ERROR]" else IDS[orientation]?.third ?: "unknown($orientation)"

        view.setCharSequence(R.id.tv_orientation, "setText", sort)
        return Notification.Builder(this, "rc")
            .setSubText(long)
            .setCustomBigContentView(view)
            .setSmallIcon(R.drawable.baseline_screen_rotation_24)
            .build()
    }
}