package com.ncm.autoclaim.calib

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import org.json.JSONObject

object CalibrationManager {
    private const val PREFS = "calibration"
    private var context: Context? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
    }

    fun hasAny(): Boolean {
        val ctx = context ?: return false
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.contains("claim_text") || prefs.contains("claim_x")
    }

    fun startCalibration(ctx: Context) {
        Toast.makeText(ctx, "校准模式：切到网易云广告页，长按对应按钮记录", Toast.LENGTH_LONG).show()
    }

    fun saveNode(type: String, text: String?, desc: String?, resId: String?, x: Int, y: Int) {
        val ctx = context ?: return
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("${type}_text", text)
            putString("${type}_desc", desc)
            putString("${type}_resId", resId)
            putInt("${type}_x", x)
            putInt("${type}_y", y)
            apply()
        }
    }

    fun loadNode(type: String): NodeRecord? {
        val ctx = context ?: return null
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return NodeRecord(
            text = prefs.getString("${type}_text", null),
            desc = prefs.getString("${type}_desc", null),
            resId = prefs.getString("${type}_resId", null),
            x = prefs.getInt("${type}_x", -1),
            y = prefs.getInt("${type}_y", -1)
        )
    }

    data class NodeRecord(
        val text: String?,
        val desc: String?,
        val resId: String?,
        val x: Int,
        val y: Int
    )

    fun exportToClipboard(ctx: Context): String? {
        return try {
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val json = JSONObject()
            for ((key, value) in prefs.all) {
                json.put(key, value)
            }
            val str = json.toString(2)
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("ncm_calibration", str))
            str
        } catch (e: Exception) {
            null
        }
    }

    fun importFromClipboard(ctx: Context): Boolean {
        return try {
            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip ?: return false
            if (clip.itemCount == 0) return false
            val str = clip.getItemAt(0).text?.toString() ?: return false
            val json = JSONObject(str)
            val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val editor = prefs.edit().clear()
            json.keys().forEach { key ->
                when (val value = json.get(key)) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Long -> editor.putInt(key, value.toInt())
                }
            }
            editor.apply()
            true
        } catch (e: Exception) {
            false
        }
    }
}
