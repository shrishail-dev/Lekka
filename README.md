# Lekka

Lekka ("account" / "the count") is a personal expense-tracker for Android. It's built for one
person to log daily spending, keep an eye on their monthly budget, and track money that moves
outside the normal budget — money borrowed, money lent, and one-off event spending like a
wedding or birthday.

Everything lives on the device. There's no account, no server, and no cloud sync — Lekka never
sends your data anywhere.

## Features

- **Expense tracking** — amount, category, a Need/Want tag, an optional note, and a date. Pick
  from a set of default categories or create your own with a custom emoji.
- **Home** — this month's balance, budget, and spend at a glance, today/this-week totals, and
  your most recent expenses. The balance card turns red the moment you go over budget.
- **Calendar** — a month grid with a running total per day; tap a day to see just that day's
  expenses, or view the whole month.
- **Monthly Report** — category breakdown, a 3-month trend, and your top 5 expenses for the
  month.
- **Insights** — a Needs vs. Wants split so you can see how much of your spending is
  discretionary.
- **Borrowed** — track money you owe, as multiple dated entries, with a running total.
- **Lent** — the reverse: money you've lent to others, tracked the same way.
- **Events** — a separate budget for one-off occasions (a wedding, a birthday, a naming
  ceremony, anything). Event spending is tracked in its own space and never counted toward your
  monthly budget.
- **Export** — download a month's expenses as a CSV or a formatted PDF, saved to
  `Downloads/Lekka/`, with a notification you can tap to open the file straight away.
- **Daily reminder** — an optional once-a-day nudge to log your spending, with a rotating pool
  of (hopefully) funny messages.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3) for the UI, following MVVM
- **Room** for on-device storage (expenses, borrows, lent entries, events), with real migrations
  between schema versions — updates preserve your data, they never wipe it
- **DataStore Preferences** for settings (budget, currency, custom categories)
- **Navigation Compose** for screen navigation
- No backend, no analytics, no third-party network calls

## Building it

This project has no `gradlew` wrapper checked in — build with a local Gradle 8.7 install instead:

```bash
gradle assembleDebug    # unsigned/debug-signed build, for local testing
gradle assembleRelease  # signed, minified build
```

A release build needs a signing key at `keystore/keystore.properties` (gitignored — generate
your own with `keytool`, pointing `storeFile` at a `.jks` alongside it). Without that file,
`assembleRelease` still runs but the app-release.apk it produces will be unsigned/debug-signed.

## Status

This is a personal project, not published to the Play Store. Releases are distributed as APKs
via this repo's [Releases](../../releases) page.
