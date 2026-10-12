package com.tamuray.tanmatsu

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** ホーム画面アイコンから起動。フォルダ未設定なら設定画面、設定済みなら HTML をそのまま開く */
class LaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val next = if (Prefs.isReady(this)) WebActivity::class.java else SettingsActivity::class.java
        startActivity(Intent(this, next))
        finish()
    }
}
