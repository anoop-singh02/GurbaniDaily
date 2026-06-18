# Daily Gurbani — Android app for Pixel

A clean, offline daily Gurbani app with a home screen widget. A new shabad each
day, plus a shuffle button. No internet needed — everything is bundled.

## What's inside
- **App**: full-screen shabad card (Gurmukhi + transliteration + meaning + source), shuffle, share, dark mode
- **Widget**: home screen widget with the day's shabad and a ↻ shuffle button; tap the card to open the app
- **Settings**: dark mode toggle
- 15 curated shabads, fully offline

---

## How to get the APK onto your Pixel (no Android Studio needed)

You'll use GitHub to build the APK in the cloud, then download and install it.

### Step 1 — Create a GitHub repo
1. Go to https://github.com and sign in (make a free account if needed).
2. Click the **+** (top right) → **New repository**.
3. Name it `GurbaniDaily`. Leave it **Public** (or Private — both work). Don't add a README. Click **Create repository**.

### Step 2 — Upload these files
On the new empty repo page:
1. Click **uploading an existing file**.
2. Drag in **everything** from the `GurbaniDaily` folder — keep the folder structure intact. (Easiest: upload the whole folder; GitHub preserves subfolders.)
3. Scroll down, click **Commit changes**.

> Important: the hidden `.github` folder must be included — that's what triggers the build. If your file browser hides dotfiles, enable "show hidden files" before uploading.

### Step 3 — Let GitHub build it
1. Go to the **Actions** tab in your repo.
2. You'll see a workflow run called **Build APK** start automatically (takes ~3–5 min). If it asks you to enable Actions, click the green button to enable.
3. Wait for the green checkmark.

### Step 4 — Download the APK
1. Click the finished **Build APK** run.
2. Scroll to **Artifacts** at the bottom.
3. Download **GurbaniDaily-apk**. It's a `.zip` — unzip it on your phone to get `app-debug.apk`.

### Step 5 — Install on your Pixel
1. Open the `app-debug.apk` file (Files app → tap it).
2. Android will warn about installing from an unknown source — allow it for your Files/browser app.
3. Tap **Install**. Done.

### Step 6 — Add the widget
1. Long-press your home screen → **Widgets**.
2. Find **Daily Gurbani** → drag the widget to your home screen.
3. Tap the ↻ to shuffle, tap the card to open the app.

---

## Adding more shabads later
Edit `app/src/main/java/com/anoop/gurbanidaily/GurbaniData.kt`, add entries to the
list, commit. GitHub rebuilds automatically — download the new APK and reinstall.

Waheguru Ji Ka Khalsa, Waheguru Ji Ki Fateh.
