# حساباتي (hesabaty) — personal finance PWA

Seif's personal finance app. Arabic (Egyptian dialect) RTL UI. Live at
https://seifeldinnasser24-source.github.io/hesabaty/ via GitHub Pages (branch `main`, root).
Installed on his Android phone as a PWA. Talk to him in Egyptian Arabic.

## Files
- `index.html` — the whole app: one self-contained file, vanilla JS, no frameworks, no build step.
- `sw.js` — service worker (offline). Navigation is network-first, so a new `index.html` reaches
  users on next open. **Bump `CACHE` (`hesabaty-vN`) whenever you change `sw.js`, the icons or the manifest.**
- `manifest.webmanifest`, `icon-*.png`, `favicon-96.png` — install metadata. The manifest
  `<link>` must stay static in `<head>` (a JS-injected link broke Chrome's install prompt).
- Notifications: always go through `devNotify()` → `serviceWorker.ready.showNotification`.
  `new Notification()` throws silently on Android Chrome. A web app can't ring while fully
  closed (no push server); note reminders ring in-app (alarm screen + Web Audio) and the 📅
  button exports them to Google Calendar for closed-app alerts.

## Android APK (Capacitor) — `android-app/`
- Same `index.html` runs inside the APK. `NATIVE` (window.Capacitor) switches on: real Android
  alarms via @capacitor/local-notifications (`nativeSync()` rebuilds the schedule after every
  save: notes on channel `alarms` with `res/raw/alarm.wav`, money due dates at 10:00 on `dues`),
  notification actions done/snooze, back button (@capacitor/app), file export through
  Filesystem+Share (`dl()`), external links via `openExt()`, in-app update check.
- Note reminders use our own native plugin `HesabatySys` (`android-app/java/*.java`, copied in by
  `customize.sh`): `AlarmManager.setAlarmClock` (survives Doze/OEM battery savers better than plain
  exact alarms), insistent alarm-sound notification with full-screen intent, Done/Snooze buttons
  that work with the app closed (stored as "acks", applied by `nativeAcks()` on next open), boot
  reschedule, plus battery-optimisation / auto-start / full-screen shortcuts (`bgGuide()`).
  Money due dates still go through @capacitor/local-notifications.
- `.github/workflows/apk.yml` builds on every push touching `index.html`/`android-app/**`:
  `npx cap add android` → `customize.sh <run_number>` (version, signing, permissions, icons,
  sound, splash) → `gradlew assembleRelease` → GitHub release `v<run>` with `hesabaty.apk`.
  `APP_BUILD` is stamped at build time; the app offers updates when a newer release exists.
- Signing key `android-app/keystore/hesabaty.jks` must never change, or updates won't install
  over the old app (and he'd lose data by uninstalling). The Android SDK can't be downloaded
  in Claude's sandbox; check builds via `gh api repos/<repo>/actions/runs`.
- Web/PWA data and APK data are separate storages; moving between them = backup + restore.

## Data — never lose it
- User data lives only in the browser on his device: `localStorage['hesabaty_v1']`, moved
  automatically to IndexedDB (`hesabaty_fs` db, key `data`) when it grows (marker `__idb__`).
- Any schema change must be backward compatible: add fields with defaults in `defaults()`;
  `normalize()` merges old saves. Never rename/remove existing fields or change their meaning.
- Daily restore points, undo stack, optional file autosave (File System Access), backup
  export/import and share. Keep all of them working.

## Domain model (state `S`)
accounts (cash/wallet/instapay/bank, optional `cur` + `S.rates`, `opening`+`openDate`),
txs (exp/inc/trf/adj/debt/gam/asset/goal; `sign` for linked types; future-dated = scheduled,
excluded from balances until their date), debts (+payments, optional monthly installments),
subs (recurring: kind exp/inc/trf, optional auto), gams (جمعيات, organizer mode with grid,
`orgAcc`, payouts), assets (USD/gold, realized P/L in `sales`), goals, notes (reminders),
boards (planning canvas: nodes/edges/strokes), budgets, people (phones for WhatsApp).
Home screen is per-month with carry-forward computed from transactions (never stored).

## How Seif works with you
- He asks for changes in Arabic; **when he says "قولي الأول" / asks for a review, propose
  and wait for approval before editing.**
- Test before shipping: load `index.html` in jsdom (or Playwright/Chromium at 390×844 for
  mobile visuals) and exercise the changed flows plus the main screens; zero console errors.
- Ship by committing to `main` and pushing — Pages redeploys in ~1–3 min. Tell him to
  refresh the app (twice) to pick it up.
