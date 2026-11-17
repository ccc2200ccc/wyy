package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

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
            val filter = IntentFilter("com.ncm.autoclaim.CLICK")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 12+ 必须指定导出标志，否则 SecurityException 闪退
                registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(receiver, filter)
            }
            receiverRegistered = true
        }
    }

    private fun tap(x: Int, y: Int) {
        val root = rootInActiveWindow ?: return
        val targetTexts = listOf("领取", "看广告", "去逛逛", "关闭")
        var node: AccessibilityNodeInfo? = null
        
        for (text in targetTexts) {
            val list = root.findAccessibilityNodeInfosByText(text)
            if (list.isNotEmpty()) {
                node = list.first()
                list.drop(1).forEach { it.recycle() } // 回收多余节点
                break
            }
        }
        
        if (node == null) {
            node = findNodeAt(root, x, y)
        }
        
        node?.let {
            if (it.isClickable) it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            it.recycle() // 必须回收，防 Binder 泄漏
        }
        root.recycle()
    }

    private fun findNodeAt(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        val bounds = android.graphics.Rect()
        root.getBoundsInScreen(bounds)
        if (root.childCount == 0 && bounds.contains(x, y) && root.isClickable) return root
        
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findNodeAt(child, x, y)
            if (found != null) return found
            child.recycle()
        }
        return null
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
