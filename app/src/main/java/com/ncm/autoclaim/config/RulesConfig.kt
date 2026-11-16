package com.ncm.autoclaim.config

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object RulesConfig {
    var claimKeywords: List<String> = listOf("领取", "免费领取")
    var jumpKeywords: List<String> = listOf("去逛逛", "去逛街")
    var closeKeywords: List<String> = listOf("关闭", "×", "跳过")
    var browseSeconds: Int = 15

    fun loadFromAssets(context: Context) {
        try {
            val input = context.assets.open("rules.json")
            val reader = BufferedReader(InputStreamReader(input))
            val json = JSONObject(reader.readText())
            claimKeywords = json.optJSONArray("claim_keywords")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: claimKeywords
            jumpKeywords = json.optJSONArray("jump_keywords")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: jumpKeywords
            closeKeywords = json.optJSONArray("close_keywords")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: closeKeywords
            browseSeconds = json.optInt("browse_seconds", 15)
        } catch (e: Exception) {
            // 保留默认值
        }
    }
}
