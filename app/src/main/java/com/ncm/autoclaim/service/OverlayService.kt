package com.ncm.autoclaim.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var button: Button? = null
    private val handler = Handler(Looper.getMainLooper())

    // 坐标
    private val entryX = 1122; private val entryY = 243
    private val claimX = 449;  private val claimY = 882
    private val close1X = 619; private val close1Y = 2143
    private val close2X = 1053; private val close2Y = 1338

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // 检查悬浮窗权限，没有就结束（不闪退）
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        showButton()
    }

    private fun showButton() {
        if (button != null) return
        button = Button(this).apply {
            text = "领歌"
            setBackgroundColor(Color.parseColor("#CC2196F3"))
            setTextColor(Color.WHITE)
            setOnClickListener { startAutoClick() }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 30; y = 200
        }
        try {
            wm.addView(button, params)
        } catch (e: Exception) {
            // 容错，避免 addView 异常闪退
            button = null
        }
    }

    private fun startAutoClick() {
        val btn = button ?: return
        btn.text = "执行中.."
        btn.setBackgroundColor(Color.parseColor("#CCC62828"))

        sendClick(entryX, entryY)
        handler.postDelayed({
            sendClick(claimX, claimY)
            handler.postDelayed({
                sendClick(close1X, close1Y)
                handler.postDelayed({
                    sendClick(close2X, close2Y)
                    handler.postDelayed({
                        btn.text = "完成✓"
                        btn.setBackgroundColor(Color.parseColor("#CC4CAF50"))
                    }, 500)
                }, 800)
            }, 800)
        }, 5200)
    }

    private fun sendClick(x: Int, y: Int) {
        val intent = Intent("com.ncm.autoclaim.CLICK").apply {
            putExtra("x", x)
            putExtra("y", y)
        }
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        button?.let {
            try { wm.removeView(it) } catch (e: Exception) {}
        }
        button = null
    }
}
