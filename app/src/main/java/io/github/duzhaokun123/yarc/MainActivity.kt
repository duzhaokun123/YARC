package io.github.duzhaokun123.yarc

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "need root and notification permission\n" +
                    "if not work, grant permission and restart\n" +
                    "S  - sensor\n" +
                    "FS - full sensor\n" +
                    "L  - landscape\n" +
                    "RL - reverse landscape\n" +
                    "SL - sensor landscape\n" +
                    "P  - portrait\n" +
                    "RP - reverse portrait\n" +
                    "SP - sensor portrait\n" +
                    "U  - unspecified"
        })
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            startService(Intent(this, ForegroundService::class.java))
        } else {
            Toast.makeText(this, "need notification permission", Toast.LENGTH_SHORT).show()
        }
    }
}