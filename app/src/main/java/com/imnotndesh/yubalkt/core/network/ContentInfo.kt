package com.imnotndesh.yubalkt.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Metadata about a YouTube track, playlist, or album fetched from the yubal server.
 *
 * Matches the TypeScript type from the extension:
 * ```
 * type ContentInfo = {
 *   title: string;
 *   artist: string;
 *   kind: "album" | "playlist" | "track";
 *   year: number | null;
 *   track_count: number | null;
 *   thumbnail_url: string | null;
 * };
 * ```
 */
@Serializable
data class ContentInfo(
    val title: String,
    val artist: String,
    val kind: ContentKind,
    val year: Int? = null,
    val track_count: Int? = null,
    val thumbnail_url: String? = null,
)

@Serializable
enum class ContentKind {
    @SerialName("album") ALBUM,
    @SerialName("playlist") PLAYLIST,
    @SerialName("track") TRACK,
}