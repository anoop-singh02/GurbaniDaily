# Daily Gurbani — What's New

Releases are tagged `build-N` so old in-app updaters can detect them. The APK's Android `versionCode` is generated from the same build number. Newest first.

## v4.17 · build-17 — 2026-07-02
- Confirm Punjabi month Sangrand notifications are wired in Settings.
- Add an explicit notification permission path so monthly Nanakshahi notifications can fire even if daily reminders were never enabled.

## v4.16 · build-16 — 2026-07-02
- Restore `build-N` GitHub tags so already-installed build 11 can detect the update.
- Keep the fixed Android `versionCode`, so once build 16 is installed the same update will not be offered again.

## v4.15 · build-15 — 2026-07-02
- Fix update detection so the APK's Android version code is generated from the GitHub Actions build number.
- Added parser support for both old `build-N` tags and version-style tags.

## v4.3 · build-14 — 2026-06-19
- **"What's New" dialog** pops up the first time you open the app after an update, summarising exactly what changed.
- Same content also lives at **Settings → Updates → What's new** anytime.
- CHANGELOG.md now ships bundled inside the APK, and the GitHub Release notes are auto-generated from it so you can also read what's new in Obtainium.
- **Auto update check** — a quiet background worker pings GitHub once a day; if a newer build is out, you get a low-priority notification in the "App Updates" channel. No more manual "Check for updates".

## v4.2 · build-13 — 2026-06-19
- Widget uses its full height for the English quote instead of ellipsizing at 5 lines with empty space below.
- Widget source line wraps to 2 lines so long attributions fit.
- Fixed "Raag Raag …" duplication — the raag prefix is now added only once whether or not BaniDB returns it prefixed.

## v4.1 · build-12 — 2026-06-19
- **Nanakshahi sangrand notification.** You get a high-priority notification on the first day of every Punjabi month (Chet ਚੇਤ, Vaisakh ਵੈਸਾਖ, Jeth ਜੇਠ, Harh ਹਾੜ, Sawan ਸਾਵਣ, Bhadon ਭਾਦੋਂ, Assu ਅੱਸੂ, Katak ਕੱਤਕ, Maghar ਮੱਘਰ, Poh ਪੋਹ, Magh ਮਾਘ, Phagun ਫੱਗਣ). Fires around 8 AM local time, includes a short greeting.
- Today's Quote subtitle now shows the current Punjabi month, e.g. "Thursday, 19 June · ਹਾੜ Harh".
- Widget ↻ actually re-rolls to a new random shabad now (previously only re-drew the cache).

## v4.0 · build-11 — 2026-06-19
- **Internet-only** — deleted the bundled 65 shabads and the Nitnem/Sukhmani/Saloks/Wisdom categories. Everything is pulled from BaniDB (Sri Guru Granth Sahib Ji) live and cached.
- **Fixed Gurmukhi rendering** — every BaniDB parser now uses the `unicode` field. Previously Gurmukhi was showing as unreadable AnmolLipi ASCII (e.g. `qU pRB dwqw…`). Now proper ਗੁਰਮੁਖੀ everywhere.
- **Today's Quote never repeats.** DailyQuote tracks seen shabad IDs on-device and re-rolls until it finds an unseen one.
- **Removed all YouTube Listen buttons** across Quote, Reader, and Hukamnama.
- **Removed Roman transliteration.** Shabads show Unicode Gurmukhi + English meaning (+ Punjabi meaning for Hukamnama), nothing else.
- **Hukamnama daily from Sri Harmandir Sahib** — cache invalidates on date change so the tab is always current.
- **Widget shows English only**, no Gurmukhi block. Updated daily by WorkManager.
- **Notifications** pull today's English quote from BaniDB.
- **Library redesigned** — three tiles (Random Shabad, Today's Hukamnama, Browse by Raag) and a search prompt.
- **Search** is online-only, spans the whole SGGS Ji.
- **Fav / History** rendered from a local preview cache so no re-fetch on open.

## v3.2 · build-10 — 2026-06-19
- **In-app auto-updater.** Settings → Updates → Check + Download + Install. FileProvider-backed; no more Obtainium needed.
- **Hukamnama archive** — date picker, browse any past day.
- **Hukamnama verse-by-verse layout** — each verse rendered with Gurmukhi + transliteration + meaning + hairline separator.
- **Browse SGGS Ji by Raag** — Library → Browse by Raag → tap raag → swipe through angs → tap any shabad to read.
- **Daily Nitnem** quick-launch chips on Library.
- **🔥 Reading streak** chip on Today's Quote (shown once streak ≥ 2 days).
- **Reflection journal** on every shabad (private, autosaves).
- **Backup export/import** via system file picker.
- **Lock-screen widget** hint for Android 17 launchers.

## v3.1 · build-8 — 2026-06-19
- Tap any shabad row (Favourites / History / Search / Library category) opens the Reader — no longer jumps straight to YouTube.
- **Search now has "Online · Full SGGS Ji" toggle** — search the whole Guru Granth Sahib Ji via BaniDB.
- Fixed top-bar overlap on Quote / Hukamnama / Library.
- Bottom-nav "Hukamnama" shortened to "Hukam" so the pill fits.
- Removed the Listen button from the Today's Quote card.

## v3.0 · build-6 — 2026-06-18
- **Three tabs** — Today (opens first), Hukamnama, Library.
- **Redesigned visual language** — warm cream + burnt saffron palette, serif display headings, gradient backdrop, pill-shaped floating bottom nav.
- **Hukamnama tab** pulls live Hukamnama Sahib from BaniDB.
- **Library** groups shabads by Nitnem / Sukhmani / Saloks / Wisdom.
- **Custom splash** — Ik Onkar logo.

## v2.1 · build-5 — 2026-06-18
- **Listen on YouTube** button on every shabad — opens YouTube with a kirtan search for that shabad's opening line + source.
- **Three reminder slots** — Amritvela (5:00), Midday (12:30), Evening / Rehras (18:30). Each independent on/off + time.

## v2.0 · build-3 — 2026-06-18
- **Material You** dynamic colour.
- **Edge-to-edge** layout.
- **Splash screen API** with Ik Onkar.
- **Swipeable HorizontalPager** of shabads.
- **Pull-to-refresh** shuffle, haptic feedback.
- **DataStore** for favourites, history (last 30), font scale, theme, reminder time.
- **Daily notification** via WorkManager + boot rescheduling.
- **Search** across gurmukhi / transliteration / meaning / source / tags.
- **Settings** with reminder time picker, dark/dynamic-colour toggles, font size slider.
- Shabad pack expanded from 15 → 65 (deleted in 4.0).

## v1.0 · build-1 — 2026-06-18
- Initial release. 15 curated shabads, full-screen shabad card (Gurmukhi + transliteration + meaning + source), shuffle, share, dark mode. Home-screen widget with day's shabad and shuffle button.
