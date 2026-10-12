package com.tamuray.tanmatsu

import android.content.Context
import android.net.Uri

/**
 * 設定の保存・読み出し。
 *  - folder   : HTML を置いた端末内フォルダ（フォルダ選択画面で選んだもの）
 *  - startFile: 最初に開くファイル名（例: index.html）
 */
object Prefs {
    private const val FILE = "launcher"
    private const val KEY_FOLDER = "folder"
    private const val KEY_START = "start_file"
    const val DEFAULT_START = "index.html"

    private fun sp(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun folder(context: Context): Uri? =
        sp(context).getString(KEY_FOLDER, null)?.let { Uri.parse(it) }

    fun startFile(context: Context): String =
        sp(context).getString(KEY_START, DEFAULT_START)?.ifBlank { DEFAULT_START } ?: DEFAULT_START

    fun save(context: Context, folder: Uri, startFile: String) {
        sp(context).edit()
            .putString(KEY_FOLDER, folder.toString())
            .putString(KEY_START, startFile.trim().ifBlank { DEFAULT_START })
            .apply()
    }

    /** フォルダと最初のファイルが両方そろっていれば true */
    fun isReady(context: Context): Boolean = folder(context) != null
}
