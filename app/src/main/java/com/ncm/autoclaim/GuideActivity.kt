package com.ncm.autoclaim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ncm.autoclaim.calib.CalibrationManager
import com.ncm.autoclaim.service.OverlayService
import com.ncm.autoclaim.util.PermissionChecker

class GuideActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)
        container = findViewById(R.id.guide_container)
        CalibrationManager.init(this)
        refresh()
    }

    private fun refresh() {
        container.removeViews(2, container.childCount - 2)

        addStep(1, "无障碍服务", PermissionChecker.isAccessibilityEnabled(this)) {
            startActivity(PermissionChecker.intentAccessibility(this))
        }
        addStep(2, "悬浮窗权限", PermissionChecker.isOverlayEnabled(this)) {
            startActivity(PermissionChecker.intentOverlay(this))
        }
        addStep(3, "电池优化白名单", PermissionChecker.isBatteryOptimizationIgnored(this)) {
            try {
                startActivity(PermissionChecker.intentBatteryOptimization(this))
            } catch (e: Exception) {
                Toast.makeText(this, "请手动到 设置→电池→忽略电池优化", Toast.LENGTH_LONG).show()
            }
        }
        addStep(4, "节点校准", CalibrationManager.hasAny()) {
            startService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "悬浮窗已启动，去网易云长按圆点校准", Toast.LENGTH_LONG).show()
        }

        val allOk = PermissionChecker.isAccessibilityEnabled(this)
                && PermissionChecker.isOverlayEnabled(this)
                && PermissionChecker.isBatteryOptimizationIgnored(this)

        val btnDone = Button(this).apply {
            text = if (allOk) "✅ 全部完成，进入主界面" else "🔄 刷新状态"
            setOnClickListener {
                if (allOk) {
                    getSharedPreferences("config", MODE_PRIVATE)
                        .edit().putBoolean("guided", true).apply()
                    startActivity(Intent(this@GuideActivity, MainActivity::class.java))
                    finish()
                } else {
                    refresh()
                }
            }
        }
        container.addView(btnDone)

        if (allOk) {
            val startBtn = Button(this).apply {
                text = "🚀 启动悬浮窗"
                setOnClickListener {
                    startService(Intent(this@GuideActivity, OverlayService::class.java))
                    Toast.makeText(this@GuideActivity, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
                }
            }
            container.addView(startBtn)
        }
    }

    private fun addStep(num: Int, name: String, ok: Boolean, onClick: () -> Unit) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }
        val status = TextView(this).apply {
            text = if (ok) "✅" else "❌"
            textSize = 18f
            setPadding(0, 0, 24, 0)
        }
        val label = TextView(this).apply {
            text = "$num. $name"
            textSize = 15f
        }
        val action = Button(this).apply {
            text = if (ok) "已授权" else "去设置"
            isEnabled = !ok
            setOnClickListener { onClick() }
        }
        row.addView(status)
        row.addView(label)
        row.addView(action)
        container.addView(row)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}
