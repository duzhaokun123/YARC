package io.github.duzhaokun123.yarc

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.topjohnwu.superuser.ipc.RootService

class WindowService: RootService() {
    companion object {
        const val MSG_UPDATE = 1
    }

    internal class MessengerHandler(
       private val windowService: WindowService
    ): Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            return when(msg.what) {
                MSG_UPDATE -> {
                    if (msg.arg2 != 2) return
                    windowService.windowManager.updateViewLayout(windowService.view, RotationParam(msg.arg1))
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    private val messenger by lazy { Messenger(MessengerHandler(this)) }
    val windowManager by lazy { getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    val view by lazy { View(this) }

    override fun onBind(intent: Intent): IBinder {
        if (Process.myUid() != Process.SYSTEM_UID)
            Process::class.java.getMethod("setUid", Int::class.javaPrimitiveType).invoke(null, Process.SYSTEM_UID)
        Toast.makeText(this, "[YARC] root service started", Toast.LENGTH_SHORT).show()
        windowManager.addView(view, RotationParam(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED))
        return messenger.binder
    }
}