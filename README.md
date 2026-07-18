<div align="center">
<img width="300" height="300" alt="yubalkt" src="https://github.com/user-attachments/assets/31bb0359-f881-411b-aa12-b9cce291b6a7" />

# YubalKt

**A native Android companion for your self-hosted [yubal](https://github.com/) server.**

Queue downloads, manage playlist subscriptions, and monitor jobs — all from your phone, styled to match the yubal web UI.

</div>

## Overview

[yubal](https://github.com/guillevc/yubal) is a self-hosted service (powered by `yt-dlp` and `ytmusicapi`) for downloading and syncing music from YouTube/YouTube Music. This app is a native Android client for it — no browser required. Share a link from any app, queue a download, subscribe to a playlist for automatic syncing, and keep an eye on progress with live job cards and push notifications.

The UI is built entirely in Jetpack Compose and intentionally mirrors the visual language of the yubal web dashboard — same teal accent palette, monospace technical labels, pill badges, and bordered panel cards — so switching between the web UI and the app feels seamless.

> [!Note]
> This app requires an active self-hosted instance before use
## Features

### Downloads
- Queue individual tracks or full albums/playlists by URL
- Live-polling job list with animated wavy progress bars
- Per-job status (Pending, Fetching Info, Downloading, Importing, Completed, Failed, Cancelled)
- Cancel active jobs or delete finished ones
- Tap a failed job to view its server logs inline
- One-tap "Clear" to wipe finished jobs
- Pull-to-refresh and automatic background polling

### Playlist Subscriptions
- Subscribe to any playlist or album URL for automatic recurring sync
- At-a-glance stats: active subscription count and one-tap "Sync all now"
- Per-subscription manual sync, last-synced timestamp, and delete
- Thumbnail-backed subscription table, closely mirroring the web dashboard

### Share Sheet Integration
- Share any YouTube / YouTube Music link into the app from any other app (browser, YT app, etc.)
- Instant content preview (title, artist, thumbnail, kind) before committing
- One tap to download, or subscribe if it's an album/playlist

### Notifications
- Ongoing download jobs surface as persistent notifications with live progress
- Automatically cleared once all active jobs finish or are cancelled

### Settings
- Upload a `cookies.txt` (Netscape format) to unlock private playlists and age-restricted content
- View/clear cookie configuration status at a glance
- Browse raw server logs in a terminal-style panel
- Logout clears the saved server connection and returns to setup

### Setup & Connection
- Simple one-time server URL configuration
- Built-in "Test Connection" health check before saving
- Automatically reconnects and resumes polling on relaunch

## Getting Started

### Prerequisites
- A running [yubal](https://github.com/) server instance, reachable from your device (default port `8000`)
- Android Studio (Koala or newer recommended)
- A device or emulator running Android 8.0 (API 26)+

### Build & Run
```bash
git clone <this-repo-url>
cd yubal-android
./gradlew installDebug
```

Or open the project in Android Studio and hit **Run**.

### First-time Setup
1. Launch the app — you'll land on the **Server Setup** screen
2. Enter your yubal server URL (e.g. `http://192.168.1.50:8000`)
3. Tap **Test** to confirm connectivity, then **Save & Connect**
4. You're in — start queuing downloads or subscribing to playlists


## Sharing Links Into the App

From YouTube, YouTube Music, or your browser, use the native **Share** action and select **yubal**. A preview sheet pops up with the content's title, artist, and thumbnail, letting you queue a download or subscription in one tap.

## Acknowledgements

Built on top of [yubal](https://github.com/guillevc/yubal) — powered by [`yt-dlp`](https://github.com/yt-dlp/yt-dlp) and [`ytmusicapi`](https://github.com/sigma67/ytmusicapi).

<div align="center">

Made to pair with the yubal web dashboard — same server, same style, now in your pocket.

</div>
