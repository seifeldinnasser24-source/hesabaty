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
