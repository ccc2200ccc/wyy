package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
            registerReceiver(receiver, IntentFilter("com.ncm.autoclaim.CLICK"))
            receiverRegistered = true
        }
    }

    // 保底方案：节点点击（不依赖 GestureDescription）
    private fun tap(x: Int, y: Int) {
        val root = rootInActiveWindow ?: return
        // 优先按文本找（领取/看广告），找不到则按坐标找节点
        val targetTexts = listOf("领取", "看广告", "去逛逛", "关闭")
        var node: AccessibilityNodeInfo? = null
        
        for (text in targetTexts) {
            val list = root.findAccessibilityNodeInfosByText(text)
            if (list.isNotEmpty()) {
                node = list.first()
                break
            }
        }
        
        if (node == null) {
            // 坐标兜底：遍历节点找包含坐标的可点击项
            node = findNodeAt(root, x, y)
        }
        
        node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun findNodeAt(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        if (root.childCount == 0) {
            val bounds = android.graphics.Rect()
            root.getBoundsInScreen(bounds)
            if (bounds.contains(x, y) && root.isClickable) return root
            return null
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findNodeAt(child, x, y)
            if (found != null) return found
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
