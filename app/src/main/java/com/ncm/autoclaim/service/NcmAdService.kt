package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.GestureDescription

class NcmAdService : AccessibilityService() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.ncm.autoclaim.CLICK") {
                val x = intent.getIntExtra("x", 0)
                val y = intent.getIntExtra("y", 0)
                tap(x, y)
            }
        }
    }

    private var receiverRegistered = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        if (!receiverRegistered) {
            registerReceiver(receiver, IntentFilter("com.ncm.autoclaim.CLICK"))
            receiverRegistered = true
        }
    }

    private fun tap(x: Int, y: Int) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (receiverRegistered) {
            try { unregisterReceiver(receiver) } catch (e: Exception) {}
            receiverRegistered = false
        }
    }
}
