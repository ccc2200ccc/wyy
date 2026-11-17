package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import com.ncm.autoclaim.MainActivity

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var button: Button
    private val handler = Handler(Looper.getMainLooper())

    // 你的坐标（四舍五入转Int）
    private val entryX = 1122; private val entryY = 243
    private val claimX = 449;  private val claimY = 882
    private val close1X = 619; private val close1Y = 2143
    private val close2X = 1053; private val close2Y = 1338

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        showButton()
    }

    private fun showButton() {
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
        wm.addView(button, params)
    }

    private fun startAutoClick() {
        button.text = "执行中.."
        button.setBackgroundColor(Color.parseColor("#CCC62828"))

        // 1. 点右上角广告入口
        sendClick(entryX, entryY)

        // 2. 等5秒广告，点领取
        handler.postDelayed({
            sendClick(claimX, claimY)

            // 3. 点第一次关闭
            handler.postDelayed({
                sendClick(close1X, close1Y)

                // 4. 点第二次关闭
                handler.postDelayed({
                    sendClick(close2X, close2Y)

                    // 完成
                    handler.postDelayed({
                        button.text = "完成✓"
                        button.setBackgroundColor(Color.parseColor("#CC4CAF50"))
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
        if (::button.isInitialized) wm.removeView(button)
        handler.removeCallbacksAndMessages(null)
    }
}
