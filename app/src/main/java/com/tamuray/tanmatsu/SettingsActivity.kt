package com.tamuray.tanmatsu

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/** 起動先アドレスの入力画面（初回起動時、またはアイコン長押し →「設定」） */
class SettingsActivity : Activity() {

    private lateinit var urlInput: EditText
    private lateinit var inAppRadio: RadioButton
    private lateinit var browserRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        root.addView(TextView(this).apply {
            text = "起動先アドレス（URL）"
            textSize = 16f
        })
        urlInput = EditText(this).apply {
            hint = "https://example.com"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            setSingleLine()
            setText(Prefs.url(this@SettingsActivity))
        }
        root.addView(urlInput, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        root.addView(TextView(this).apply {
            text = "開き方"
            textSize = 16f
            setPadding(0, pad, 0, 0)
        })
        inAppRadio = RadioButton(this).apply { text = "アプリ内で開く"; id = 1 }
        browserRadio = RadioButton(this).apply { text = "ブラウザ／対応アプリで開く"; id = 2 }
        root.addView(RadioGroup(this).apply {
            addView(inAppRadio)
            addView(browserRadio)
            check(if (Prefs.inApp(this@SettingsActivity)) 1 else 2)
        })

        root.addView(Button(this).apply {
            text = "保存して開く"
            setOnClickListener { saveAndOpen() }
        }, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = pad })

        root.addView(TextView(this).apply {
            text = "※ 後から変更するときは、ホーム画面のアイコンを長押し →「設定」"
            textSize = 13f
            setPadding(0, pad, 0, 0)
        })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun saveAndOpen() {
        val url = Prefs.normalize(urlInput.text.toString())
        if (url.isEmpty()) {
            Toast.makeText(this, "アドレスを入力してください", Toast.LENGTH_SHORT).show()
            return
        }
        val inApp = inAppRadio.isChecked
        Prefs.save(this, url, inApp)
        if (Prefs.open(this, url, inApp)) finish()
    }
}
