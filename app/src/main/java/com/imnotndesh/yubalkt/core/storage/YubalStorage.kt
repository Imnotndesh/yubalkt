package com.imnotndesh.yubalkt.core.storage

interface YubalStorage {

    /**
     * The configured yubal server instance URL.
     * This is persisted across app restarts (equivalent to the
     * extension's `sync:yubalUrl`).
     *
     * Example: `"http://192.168.1.42:8000"` or `"https://yubal.example.com"`
     *
     * @return The stored URL, or `null` if not configured.
     */
    suspend fun getInstanceUrl(): String?

    /**
     * Persist the yubal server instance URL.
     */
    suspend fun saveInstanceUrl(url: String)

    /**
     * Clear the stored instance URL (effectively resetting setup).
     */
    suspend fun clearInstanceUrl()

    /**
     * A draft URL used transiently during the setup screen.
     * Unlike the extension's `session:yubalUrlDraft`, this is
     * in-memory and not persisted — it avoids polluting storage
     * with half-edited values.
     */
    fun getDraftUrl(): String?

    /**
     * Update the in-memory draft URL as the user types.
     */
    fun setDraftUrl(url: String?)

    /**
     * Clear the draft URL (on save or discard).
     */
    fun clearDraftUrl()

    /**
     * Whether the user has configured a yubal instance.
     */
    suspend fun isConfigured(): Boolean
}
