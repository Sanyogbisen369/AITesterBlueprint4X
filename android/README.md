# Free Play Picks: Android app (Play Store package)

This folder is the Android app for the [Free Play Picks](../Android_Games_Hub/) web app.
It is a **Trusted Web Activity (TWA)**: a small native app that opens the hosted site
full-screen through Chrome, so every change you merge to the web app reaches Play Store
users without a new release.

- Package name: `io.github.sanyogbisen369.freeplaypicks`
- Opens: https://sanyogbisen369.github.io/AITesterBlueprint4X/Android_Games_Hub/
- Built with [android-browser-helper](https://github.com/GoogleChrome/android-browser-helper), target SDK 36

## How the app gets built

You don't need Android Studio. The **Android app** GitHub workflow
(`.github/workflows/android.yml`) builds it on GitHub's servers whenever `android/` changes,
or when you run it by hand (Actions → Android app → Run workflow).

Each run produces, under the run's **Artifacts** section:

| Artifact | What it is | Needs secrets |
| --- | --- | --- |
| `free-play-picks-debug-apk` | Test build you can install on your phone | No |
| `free-play-picks-release-aab` | Signed bundle to upload to the Play Console | Yes |

The version code is the workflow run number, so every upload has a higher version.

## One-time setup

### 1. Add the signing key secrets
Your upload key (`upload-keystore.jks`) and its passwords were given to you separately. **They
are not in this repo and must never be committed.** Back them up somewhere safe.

In GitHub: **Settings → Secrets and variables → Actions → New repository secret**, add:

| Secret | Value |
| --- | --- |
| `UPLOAD_KEYSTORE_BASE64` | Full contents of `upload-keystore.base64.txt` |
| `KEYSTORE_PASSWORD` | Store password |
| `KEY_ALIAS` | `upload` |
| `KEY_PASSWORD` | Key password |

Then run the workflow again to get the signed `.aab`.

### 2. Create the app in the Play Console
1. Sign up at https://play.google.com/console (one-time US$25, identity verification).
2. **Create app** → name "Free Play Picks", type **App**, **Free**.
3. Fill in the store listing and App content forms with [`play-store/listing.md`](play-store/listing.md)
   and the images in [`play-store/`](play-store/).
4. Keep **Play App Signing** turned on (default) and upload the `.aab` from the workflow.

New personal developer accounts must run a **closed test with at least 12 testers for
14 days** before they can apply for production access. Create a closed testing track,
add testers' Gmail addresses, and share the opt-in link.

### 3. Verify the website (removes the browser address bar)
Android only shows the app full-screen when the website confirms it owns the app, through a
Digital Asset Links file at **`https://sanyogbisen369.github.io/.well-known/assetlinks.json`**.
Without it the app still works, but shows a small Chrome address bar at the top.

Because the file must sit at the root of `sanyogbisen369.github.io`, it lives in a separate
repository:

1. Create a public repo named exactly **`sanyogbisen369.github.io`**.
2. Add [`assetlinks.json`](assetlinks.json) to it at `.well-known/assetlinks.json`.
3. Add an empty file named `.nojekyll` at the repo root (GitHub Pages otherwise skips folders starting with a dot).
4. Turn on Pages for that repo (Settings → Pages → Deploy from branch → `main` / root).
5. After your first upload, open Play Console → **Test and release → App integrity → App signing**
   and copy the **App signing key certificate SHA-256**. Add it to the
   `sha256_cert_fingerprints` list in `assetlinks.json`, next to the upload key fingerprint
   that is already there.

## Building locally (optional)
With JDK 17 and the Android SDK installed:

```bash
cd android
./gradlew assembleDebug          # app/build/outputs/apk/debug/
KEYSTORE_PATH=/path/upload-keystore.jks KEYSTORE_PASSWORD=... KEY_ALIAS=upload KEY_PASSWORD=... \
  ./gradlew bundleRelease        # app/build/outputs/bundle/release/
```

## Files
```
android/
├── app/
│   ├── build.gradle                 # app id, SDK levels, signing from env vars
│   └── src/main/
│       ├── AndroidManifest.xml      # TWA launcher pointing at the hosted site
│       └── res/                     # name, colours, launcher icons, splash
├── assetlinks.json                  # copy to sanyogbisen369.github.io/.well-known/
├── play-store/                      # listing text, icon, feature graphic, screenshots
├── build.gradle, settings.gradle, gradle.properties
└── gradlew, gradle/wrapper/         # Gradle 8.14.3 wrapper
```
