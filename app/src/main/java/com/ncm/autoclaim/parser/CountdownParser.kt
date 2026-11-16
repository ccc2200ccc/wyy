package com.ncm.autoclaim.parser

import android.view.accessibility.AccessibilityNodeInfo

object CountdownParser {

    /** 找到倒计时节点则返回剩余秒数，否则 null */
    fun findCountdown(root: AccessibilityNodeInfo): Int? {
        val probes = listOf("秒后领取", "倒计时", "s后", "领取(")
        for (probe in probes) {
            val nodes = root.findAccessibilityNodeInfosByText(probe)
            if (nodes.isNotEmpty()) {
                val text = nodes[0].text?.toString() ?: continue
                val match = Regex("""(\d+)""").find(text)
                return match?.groupValues?.get(1)?.toIntOrNull() ?: 4
            }
        }
        return null
    }
}
