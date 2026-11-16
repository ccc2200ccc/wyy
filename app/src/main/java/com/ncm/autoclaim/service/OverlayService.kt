package com.ncm.autoclaim.service

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var btn: Button
    private val handler = Handler(Looper.getMainLooper())

    // ===== 你的坐标（已取整）=====
    private val entryX = 1122   // 右上角广告入口
    private val entryY = 243

    private val claimX = 449    // 5秒后领取
    private val claimY = 882

    private val close1X = 619   // 第一次关闭
    private val close1Y = 2143

    private val close2X = 1053  // 第二次关闭
    private val close2Y = 1338
    // ===========================

    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        btn = Button(this).apply {
            text = "领歌"
            setBackgroundColor(Color.parseColor("#FF2196F3"))
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(30, 20, 30, 20)
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
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 400
        }

        // 拖动
        btn.setOnTouchListener(object : View.OnTouchListener {
            private var initX = 0
            private var initY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View?, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = params.x
                        initY = params.y
                        touchX = e.rawX
                        touchY = e.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initX + (e.rawX - touchX).toInt()
                        params.y = initY + (e.rawY - touchY).toInt()
                        wm.updateViewLayout(btn, params)
                        return true
                    }
                }
                return false
            }
        })

        // 点击 = 开始自动流程
        btn.setOnClickListener {
            if (!isRunning) {
                startFlow()
            }
        }

        wm.addView(btn, params)
    }

    private fun startFlow() {
        isRunning = true
        btn.text = "执行中"
        btn.setBackgroundColor(Color.parseColor("#FFFF9800"))

        // ① 点广告入口
        sendClick(entryX, entryY)

        // ② 等5秒 → 点领取
        handler.postDelayed({
            sendClick(claimX, claimY)

            // ③ 等0.8秒 → 第一次关闭
            handler.postDelayed({
                sendClick(close1X, close1Y)

                // ④ 等0.8秒 → 第二次关闭
                handler.postDelayed({
                    sendClick(close2X, close2Y)

                    // 完成
                    handler.postDelayed({
                        isRunning = false
                        btn.text = "完成✓"
                        btn.setBackgroundColor(Color.parseColor("#FF4CAF50")

                        // 3秒后恢复
                        handler.postDelayed({
                            btn.text = "领歌"
                            btn.setBackgroundColor(Color.parseColor("#FF2196F3"))
                        }, 3000)
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (::btn.isInitialized) wm.removeView(btn)
        handler.removeCallbacksAndMessages(null)
    }
}
