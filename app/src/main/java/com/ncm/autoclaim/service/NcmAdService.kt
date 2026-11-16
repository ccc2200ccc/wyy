package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Path
import android.view.accessibility.AccessibilityNodeInfo

class NcmAdService : AccessibilityService() {

    private val clickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.ncm.autoclaim.CLICK" -> {
                    val x = intent.getIntExtra("x", 0)
                    val y = intent.getIntExtra("y", 0)
                    tapAt(x, y)
                }
                "com.ncm.autoclaim.BACK" -> {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        registerReceiver(clickReceiver, IntentFilter().apply {
            addAction("com.ncm.autoclaim.CLICK")
            addAction("com.ncm.autoclaim.BACK")
        })
    }

    private fun tapAt(x: Int, y: Int) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // 不需要自动监听了，纯手动触发
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(clickReceiver)
    }
}
