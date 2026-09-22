<div align="center">

# 🌊 LiquidStream

### *Next-Generation Android Movie Streaming & Downloader*
#### Powered by Jetpack Compose & Apple-Inspired Liquid Glass UI

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin%202.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Media-AndroidX%20Media3%20ExoPlayer-FF6F00?style=for-the-badge&logo=youtube&logoColor=white" alt="Media3" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" alt="License" />
</p>

<p align="center">
  <b>LiquidStream</b> is a high-performance Android movie streaming and download application engineered with a world-class <b>Apple iOS Liquid Glass</b> design system. Featuring zero-ad media stream resolution, gesture-driven ExoPlayer controls, an AI movie assistant, and an adaptive 3-tier graphics engine optimized for both flagship and budget Android phones.
</p>

---

</div>

## 🌟 Highlights at a Glance

* 💎 **Ultra-Modern iOS Liquid Glass UI** — Dynamic backdrop refraction, frosted grain noise, specular edge highlights, and fluid touch ripples.
* ⚡ **3-Tier Adaptive Performance Engine** — Automatic hardware detection ensures silky-smooth 60–120 FPS, even on budget devices with <3GB RAM.
* 🛡️ **Zero-Ad Streaming Pipeline** — Reverse-engineered multi-hop media resolver that completely strips popup ads, malicious redirects, and ad trackers.
* 🎬 **Cinematic Media3 Player** — Full-featured AndroidX ExoPlayer with swipe brightness/volume gestures, double-tap seek, and multi-resolution selection (1080p, 720p, 360p).
* 🤖 **Liquid AI Assistant** — Integrated intelligent AI chat concierge for movie recommendations, plot summaries, and mood-based suggestions.
* 📥 **Integrated Offline Downloader** — Background downloader supporting chunked downloads, pause/resume, and local media playback.
* 🎨 **Deep Customization Studio** — Real-time customization sheet allowing users to tune blur radii, tint colors, halo glows, and performance tiers.

---

## 📸 Key Features Deep-Dive

### 1. 💎 Apple iOS Liquid Glass UI Paradigm
Built strictly from scratch using custom Jetpack Compose Canvas shaders, `RenderEffect`, and AGSL:
* **Prism Dispersion (`PrismDispersionBrush`)**: Real-time chromatic rainbow refraction at card boundaries.
* **Ambient Halo Lighting (`AmbientHaloEngine`)**: Soft dynamic luminescence radiating behind hero banners and posters.
* **Frosted Grain Shader (`FrostedGrainTexture`)**: Subtle Apple-style analog noise overlay eliminating digital banding.
* **Touch-Reactive Gel Physics (`LiquidGelPhysics`)**: Interactive surface spring bounce and haptic liquid ripples upon user touch.

### 2. ⚡ 3-Tier Adaptive Quality Engine
To prevent stutter or memory exhaustion on entry-level Android devices, LiquidStream automatically evaluates device RAM, SoC capabilities, and API level:

| Tier | Target Devices | Render Strategy | Memory Impact | Target Frame Rate |
| :--- | :--- | :--- | :--- | :--- |
| **Tier 1: Ultra Liquid** | Android 12+ (API 31+), RAM > 6GB | AGSL + `RenderEffect.createBlurEffect` (25dp) + Specular refraction | Standard GPU buffer | 90–120 FPS |
| **Tier 2: Balanced Glass** | Android 10–11 (API 29–30), 4–6GB RAM | Downsampled single-pass backdrop blur (12dp) + hairline specular border | Minimal | 60 FPS |
| **Tier 3: Performance Fallback** | Low-RAM devices (`isLowRamDevice`), < 4GB RAM, or API < 29 | **Zero-Allocation Mode**: Multi-layer translucent alpha composite (`0xCC181A20`) + 1dp glass edge border | **0 MB Extra GPU Overhead** | **Rock-solid 60 FPS** |

> *Users can also manually toggle or force any tier inside the in-app Customization Studio.*

---

### 3. 🛡️ Ad-Free Reverse Engineering Engine
LiquidStream includes a secure, pure-Kotlin scraper powered by **OkHttp** and **Jsoup**:
* **Bypasses Browser Hijacks**: Web scrapers execute no client-side JavaScript, rendering pop-under scripts (`aclib.js`, `red-suspect.com`, `llvpn.com`) completely powerless.
* **Dynamic Referer Injection**: Custom `HeaderInterceptor` dynamically crafts legitimate upstream Referer, User-Agent, and tokenized session cookies (`CookieJar`).
* **HTTP 206 Byte-Range Streaming**: Enables direct, instant seekable progressive video streaming right through AndroidX Media3 without full-file buffering.

---

### 4. 🎬 Advanced Gesture-Controlled Media Player
* **Intuitive Gestures**: Vertical swipe on left side for brightness, vertical swipe on right side for volume, and horizontal swipe for track seeking.
* **Quality Switcher**: Seamlessly switch between **1080p Full HD**, **720p HD**, and **360p Data Saver** streams.
* **Aspect Ratio Modes**: Toggle between Fit, Fill, Zoom, and 16:9 widescreen stretch.
* **Picture-in-Picture (PiP)**: Seamless background playback support.

---

### 5. 🤖 Liquid AI Movie Assistant
Need recommendations for movie night? The built-in AI concierge assists users in discovering films based on:
* Mood, genre, actor, or release era
* Similar movie matchmaking
* Quick synopsis and spoiler-free trivia breakdowns

---

## 🛠️ Technology Stack & Architecture

LiquidStream is built according to modern **Clean Architecture** and Google's recommended **MVI/MVVM** guidelines:

```
app/src/main/java/com/cybersec/liquidstream/
├── core/
│   ├── glass/             # Liquid glass engine, shaders, halo lights, & physics
│   ├── network/           # OkHttp client, dynamic referer interceptors, cookies
│   └── util/              # Device RAM detection, formatters, and system utils
├── data/
│   ├── model/             # Movie, MovieDetail, StreamSource, DownloadItem, AI models
│   ├── parser/            # Reverse-engineered Moviesda multi-hop scraper
│   └── repository/        # Movie catalog, downloads, watch history & watchlist
└── ui/
    ├── components/        # Reusable liquid glass navbars, cards, shimmer skeletons
    ├── navigation/        # Jetpack Compose Navigation routes & backstack
    └── screens/           # Home, Detail, Player, Downloads, AI Chat, Search, Settings
```

| Layer | Technologies Used |
| :--- | :--- |
| **Language** | [Kotlin 2.0+](https://kotlinlang.org/) (Coroutines, StateFlow, Channel) |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) |
| **Media Playback** | [AndroidX Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3) |
| **Networking & Scraping**| [OkHttp 4](https://square.github.io/okhttp/) & [Jsoup](https://jsoup.org/) |
| **Image Loading** | [Coil Compose](https://coil-kt.github.io/coil/compose/) |
| **Asynchronous** | Kotlin Coroutines & Reactive Flows |
| **Build System** | Gradle Kotlin DSL (`build.gradle.kts`) |

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio Ladybug (2024.2+)** or newer
* **JDK 17** or **JDK 21**
* **Android SDK**: Min SDK 26 (Android 8.0) | Target SDK 35 (Android 15)

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/keerthan4531-a11y/liquid-streaming.git

# Navigate to the project directory
cd liquid-streaming

# Build debug APK using Gradle wrapper
./gradlew assembleDebug
```

The output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🔐 Disclaimer

> **Educational & Research Purpose Only**  
> This project is developed strictly for educational, architectural, and reverse-engineering research purposes to study modern Android Jetpack Compose UI rendering, adaptive shader performance, and secure HTTP media stream extraction. All media content and trademarks belong to their respective copyright holders.

---

## 👨‍💻 Author & Maintainer

Developed with ❤️ by **[Keerthan](https://github.com/keerthan4531-a11y)**

⭐ **If you find this project impressive or useful, don't forget to star the repository!**
