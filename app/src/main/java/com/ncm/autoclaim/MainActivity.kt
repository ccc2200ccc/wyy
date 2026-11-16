package com.ncm.autoclaim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ncm.autoclaim.calib.CalibrationManager
import com.ncm.autoclaim.service.OverlayService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val guided = getSharedPreferences("config", MODE_PRIVATE)
            .getBoolean("guided", false)
        if (!guided) {
            startActivity(Intent(this, GuideActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        CalibrationManager.init(this)

        findViewById<Button>(R.id.btn_start_overlay).setOnClickListener {
            startService(Intent(this, OverlayService::class.java))
            Toast.makeText(this, "悬浮窗已启动", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_export_calib).setOnClickListener {
            val json = CalibrationManager.exportToClipboard(this)
            Toast.makeText(this, if (json != null) "已导出到剪贴板" else "导出失败", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btn_import_calib).setOnClickListener {
            val ok = CalibrationManager.importFromClipboard(this)
            Toast.makeText(this, if (ok) "导入成功" else "导入失败", Toast.LENGTH_SHORT).show()
        }
    }
}
