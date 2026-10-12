package com.tamuray.tanmatsu

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import java.io.InputStream

/**
 * 設定で選んだ端末内フォルダから、"css/style.css" のような相対パスでファイルを取り出す。
 * フォルダ選択画面で得た許可（content:// の読み取り権限）を使うので、
 * ストレージ全体の権限は要らない。
 */
class LocalFolder(private val context: Context, private val tree: Uri) {

    /** パス → ドキュメントID のキャッシュ（同じファイルを毎回検索しないため） */
    private val cache = HashMap<String, String>()

    /** 相対パスのファイルを開く。見つからなければ null */
    fun open(path: String): InputStream? {
        val docId = resolve(path) ?: return null
        val uri = DocumentsContract.buildDocumentUriUsingTree(tree, docId)
        return try {
            context.contentResolver.openInputStream(uri)
        } catch (_: Exception) {
            null
        }
    }

    /** "a/b/c.html" を上から1段ずつたどってドキュメントIDに変える */
    private fun resolve(path: String): String? {
        cache[path]?.let { return it }
        var parent = DocumentsContract.getTreeDocumentId(tree)
        for (name in path.split('/').filter { it.isNotEmpty() }) {
            parent = findChild(parent, name) ?: return null
        }
        cache[path] = parent
        return parent
    }

    private fun findChild(parentDocId: String, name: String): String? {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentDocId)
        val cols = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        )
        context.contentResolver.query(children, cols, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                if (c.getString(1) == name) return c.getString(0)
            }
        }
        return null
    }

    /** ファイルを更新したあと「再読み込み」したときに、古い対応表を捨てる */
    fun clearCache() = cache.clear()

    companion object {
        /** よく使う拡張子は固定で決める（端末ごとの判定ゆれを避ける） */
        private val KNOWN = mapOf(
            "html" to "text/html", "htm" to "text/html",
            "css" to "text/css",
            "js" to "text/javascript", "mjs" to "text/javascript",
            "json" to "application/json",
            "svg" to "image/svg+xml",
            "png" to "image/png", "jpg" to "image/jpeg", "jpeg" to "image/jpeg",
            "gif" to "image/gif", "webp" to "image/webp", "ico" to "image/x-icon",
            "woff" to "font/woff", "woff2" to "font/woff2", "ttf" to "font/ttf",
            "txt" to "text/plain", "csv" to "text/csv"
        )

        fun mimeOf(path: String): String {
            val ext = path.substringAfterLast('.', "").lowercase()
            return KNOWN[ext]
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                ?: "application/octet-stream"
        }
    }
}
