package com.ncm.autoclaim.service

import android.accessibilityservice.AccessibilityService
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var button: FrameLayout
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    // 点击坐标（根据你的手机分辨率调整，这里是常见1080P的大概位置）
    // 你可以改这些数字来对准你手机上的按钮位置
    private val watchAdX = 540   // "看广告"按钮的X坐标（屏幕中间）
    private val watchAdY = 1800  // "看广告"按钮的Y坐标（底部区域）
    private val claimX = 540     // "领取"按钮的X坐标
    private val claimY = 1200    // "领取"按钮的Y坐标
    private val closeX = 980     // "关闭"按钮的X坐标（右上角）
    private val closeY = 120     // "关闭"按钮的Y坐标

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        // 创建悬浮按钮（圆形，可拖动）
        button = FrameLayout(this).apply {
            setBackgroundColor(0xFF2196F3.toInt()) // 蓝色
            layoutParams = FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.TOP or Gravity.END
            }
        }

        val params = WindowManager.LayoutParams(
            120, 120,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 0
            y = 300
        }

        // 拖动
        button.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX - (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        wm.updateViewLayout(button, params)
                        return true
                    }
                }
                return false
            }
        })

        // 点击 = 开始自动流程
        button.setOnClickListener {
            if (!running) {
                startAutoFlow()
            } else {
                Toast.makeText(this, "正在执行中...", Toast.LENGTH_SHORT).show()
            }
        }

        wm.addView(button, params)
        Toast.makeText(this, "悬浮按钮已启动，点它自动领", Toast.LENGTH_LONG).show()
    }

    private fun startAutoFlow() {
        running = true
        button.setBackgroundColor(0xFFFF9800.toInt()) // 变橙色=运行中

        // 步骤1：点"看广告"入口（你现在手动在网易云页面）
        handler.postDelayed({
            performClick(watchAdX, watchAdY)
        }, 500)

        // 步骤2：等广告加载完，点"领取"（4秒倒计时后）
        handler.postDelayed({
            performClick(claimX, claimY)
        }, 6000) // 5秒加载 + 1秒缓冲

        // 步骤3：如果没领到（可能是跳App类广告），等更久再试
        handler.postDelayed({
            // 尝试点关闭/返回
            performGlobalBack()
        }, 22000) // 等20秒后返回

        // 步骤4：回来后再点领取
        handler.postDelayed({
            handler.postDelayed({
                performClick(claimX, claimY)
                running = false
                button.setBackgroundColor(0xFF4CAF50.toInt()) // 变绿色=完成
                Toast.makeText(this, "✅ 完成！", Toast.LENGTH_SHORT).show()

                // 3秒后恢复蓝色
                handler.postDelayed({
                    button.setBackgroundColor(0xFF2196F3.toInt())
                }, 3000)
            }, 2000) // 等2秒让页面回来
        }, 24000)
    }

    private fun performClick(x: Int, y: Int) {
        // 通过无障碍服务执行点击
        val intent = Intent("com.ncm.autoclaim.CLICK")
        intent.putExtra("x", x)
        intent.putExtra("y", y)
        sendBroadcast(intent)

        // 同时用 shell 方式兜底（需要 adb 权限）
        try {
            // 方法1：用 input tap（需要 shell 权限）
            // 这里通过无障碍服务转发
        } catch (e: Exception) {}
    }

    private fun performGlobalBack() {
        val intent = Intent("com.ncm.autoclaim.BACK")
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (::button.isInitialized) wm.removeView(button)
        handler.removeCallbacksAndMessages(null)
    }
}
