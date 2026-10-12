package com.tamuray.tanmatsu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

/**
 * 設定画面（初回起動時、またはアイコン長押し →「設定」）。
 *  1. 「フォルダを選ぶ」で、HTML を置いた端末内フォルダを Android の標準フォルダ選択画面から選ぶ
 *  2. 最初に開くファイル名を入力（既定 index.html）
 *  3. 「保存して開く」
 */
class SettingsActivity : Activity() {

    companion object {
        private const val REQ_FOLDER = 10
    }

    private lateinit var folderLabel: TextView
    private lateinit var startInput: EditText
    private var folder: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folder = Prefs.folder(this)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        root.addView(TextView(this).apply {
            text = "HTML を置いたフォルダ"
            textSize = 16f
        })
        folderLabel = TextView(this).apply {
            textSize = 14f
            setPadding(0, pad / 2, 0, pad / 2)
        }
        root.addView(folderLabel)
        root.addView(Button(this).apply {
            text = "フォルダを選ぶ"
            setOnClickListener { pickFolder() }
        }, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        root.addView(TextView(this).apply {
            text = "最初に開くファイル名"
            textSize = 16f
            setPadding(0, pad, 0, 0)
        })
        startInput = EditText(this).apply {
            hint = Prefs.DEFAULT_START
            setSingleLine()
            setText(Prefs.startFile(this@SettingsActivity))
        }
        root.addView(startInput, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

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
        showFolder()
    }

    private fun showFolder() {
        val f = folder
        folderLabel.text = if (f == null) "（未選択）" else folderDisplayName(f)
    }

    /** 選んだフォルダを人が読める形にする（例: primary:Download/nippo → Download/nippo） */
    private fun folderDisplayName(uri: Uri): String {
        val id = try { DocumentsContract.getTreeDocumentId(uri) } catch (_: Exception) { return uri.toString() }
        return id.substringAfter(':', id)
    }

    private fun pickFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        startActivityForResult(intent, REQ_FOLDER)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_FOLDER || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        // アプリを再起動しても読めるように、読み取り許可を保持する
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        folder = uri
        showFolder()
    }

    private fun saveAndOpen() {
        val f = folder
        if (f == null) {
            Toast.makeText(this, "フォルダを選んでください", Toast.LENGTH_SHORT).show()
            return
        }
        Prefs.save(this, f, startInput.text.toString())
        startActivity(Intent(this, WebActivity::class.java))
        finish()
    }
}
