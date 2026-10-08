package com.tamuray.tanmachu

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** 起動先アドレスと開き方の保存・起動処理 */
object Prefs {
    private const val FILE = "launcher"
    private const val KEY_URL = "url"
    private const val KEY_IN_APP = "in_app"

    fun url(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY_URL, "") ?: ""

    fun inApp(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_IN_APP, true)

    fun save(context: Context, url: String, inApp: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString(KEY_URL, url)
            .putBoolean(KEY_IN_APP, inApp)
            .apply()
    }

    /** "example.com" のようにスキーム無しで入力された場合は https:// を補う */
    fun normalize(input: String): String {
        val s = input.trim()
        if (s.isEmpty()) return s
        return if (Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:").containsMatchIn(s)) s else "https://$s"
    }

    private fun isWeb(url: String): Boolean =
        url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)

    /** 保存済み設定に従ってアドレスを開く。開けなければ false */
    fun open(activity: Activity, url: String, inApp: Boolean): Boolean {
        if (inApp && isWeb(url)) {
            activity.startActivity(
                Intent(activity, WebActivity::class.java).putExtra(WebActivity.EXTRA_URL, url)
            )
            return true
        }
        return try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(activity, "このアドレスを開けるアプリがありません", Toast.LENGTH_LONG).show()
            false
        }
    }
}
