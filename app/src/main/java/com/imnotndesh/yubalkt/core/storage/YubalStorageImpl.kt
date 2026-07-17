package com.imnotndesh.yubalkt.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Concrete [YubalStorage] backed by Android [SharedPreferences]
 * for persistent data + a simple in-memory field for the draft URL.
 *
 * The draft URL is intentionally NOT persisted — it represents
 * a URL the user is currently editing but hasn't confirmed yet.
 * Persisting it would leave stale drafts across app restarts.
 */
class YubalStorageImpl(context: Context) : YubalStorage {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    @Volatile
    private var draftUrl: String? = null

    override suspend fun getInstanceUrl(): String? {
        return prefs.getString(KEY_INSTANCE_URL, null)
    }

    override suspend fun saveInstanceUrl(url: String) {
        prefs.edit { putString(KEY_INSTANCE_URL, url) }
    }

    override suspend fun clearInstanceUrl() {
        prefs.edit().remove(KEY_INSTANCE_URL).apply()
    }

    override suspend fun isConfigured(): Boolean {
        return prefs.contains(KEY_INSTANCE_URL)
    }

    override fun getDraftUrl(): String? = draftUrl

    override fun setDraftUrl(url: String?) {
        draftUrl = url
    }

    override fun clearDraftUrl() {
        draftUrl = null
    }

    companion object {
        private const val PREFS_NAME = "yubal_preferences"
        private const val KEY_INSTANCE_URL = "yubal_instance_url"
    }
}
