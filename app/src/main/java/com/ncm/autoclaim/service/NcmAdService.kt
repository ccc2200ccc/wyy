package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ncm.autoclaim.config.OverlayState
import com.ncm.autoclaim.config.RulesConfig
import com.ncm.autoclaim.parser.CountdownParser
import kotlinx.coroutines.*

class NcmAdService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var state = OverlayState.IDLE
    private var browseJob: Job? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        RulesConfig.loadFromAssets(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.packageName != "com.netease.cloudmusic") return
        val root = rootInActiveWindow ?: return

        when (state) {
            OverlayState.IDLE, OverlayState.SCAN -> scanForAd(root)
            OverlayState.TYPE_A -> handleTypeA(root)
            OverlayState.TYPE_B -> handleTypeB(root)
            else -> {}
        }
    }

    private fun scanForAd(root: AccessibilityNodeInfo) {
        state = OverlayState.SCAN

        // 1) 倒计时广告（Type A）
        val countdown = CountdownParser.findCountdown(root)
        if (countdown != null) {
            state = OverlayState.TYPE_A
            scheduleClick(countdown)
            return
        }

        // 2) 跳转类广告（Type B）
        val jumpNode = findNodeByKeywords(root, RulesConfig.jumpKeywords)
        if (jumpNode != null) {
            state = OverlayState.TYPE_B
            jumpNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            startBrowseTimer()
            return
        }

        // 3) 直接可领
        val claimNode = findNodeByKeywords(root, RulesConfig.claimKeywords)
        if (claimNode != null) {
            claimNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            state = OverlayState.DONE
        }
    }

    private fun handleTypeA(root: AccessibilityNodeInfo) {
        val claimNode = findNodeByKeywords(root, RulesConfig.claimKeywords)
        if (claimNode != null) {
            claimNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            state = OverlayState.DONE
        }
    }

    private fun handleTypeB(root: AccessibilityNodeInfo) {
        val claimNode = findNodeByKeywords(root, RulesConfig.claimKeywords)
        if (claimNode != null) {
            claimNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            state = OverlayState.DONE
        }
    }

    private fun scheduleClick(delaySec: Int) {
        scope.launch {
            delay(delaySec * 1000L)
            val root = rootInActiveWindow
            root?.let {
                val node = findNodeByKeywords(it, RulesConfig.claimKeywords)
                node?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            state = OverlayState.DONE
        }
    }

    private fun startBrowseTimer() {
        browseJob?.cancel()
        browseJob = scope.launch {
            delay(RulesConfig.browseSeconds * 1000L)
            performGlobalBack()
            delay(1500)
            val root = rootInActiveWindow
            root?.let {
                val claimNode = findNodeByKeywords(it, RulesConfig.claimKeywords)
                claimNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            state = OverlayState.DONE
        }
    }

    private fun performGlobalBack() {
        val path = Path().apply { moveTo(500f, 500f) }
        dispatchGesture(
            GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0, 10)).build(),
            null, null
        )
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun findNodeByKeywords(
        root: AccessibilityNodeInfo,
        keywords: List<String>
    ): AccessibilityNodeInfo? {
        for (keyword in keywords) {
            val nodes = root.findAccessibilityNodeInfosByText(keyword)
            if (nodes.isNotEmpty()) return nodes[0]
        }
        return null
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        browseJob?.cancel()
        scope.cancel()
    }
}
