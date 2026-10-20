# Free Play Picks: Android Games Hub

A mobile-first app that lists the best **free, top-rated Android games** across five categories:

| Category | What's in it |
| --- | --- |
| Combat | Shooters, battle royales, action RPGs (Call of Duty: Mobile, PUBG Mobile, Free Fire, Genshin Impact...) |
| Survival | Crafting, base building, zombies (Last Day on Earth, Grim Soul, Day R Survival, LifeAfter...) |
| Racing | Arcade, drag, drift and kart racing (Asphalt Legends Unite, CSR Racing 2, Hill Climb Racing 2...) |
| Fighting | 1v1 and team fighters (Shadow Fight 3, Mortal Kombat, Marvel Contest of Champions, Brawlhalla...) |
| In-Person | Games to play together in the same room: local Wi-Fi, pass-and-play, phones as controllers (Mini Militia, BombSquad, Spaceteam, Among Us...) |

## Features

- Filter by category, search by name, studio or feature (e.g. "offline", "co-op", "local wi-fi")
- Sort by top rated, most downloaded, or A to Z
- Save favourites to "My list" (stored on the device)
- "Pick for me" suggests a random game from the current filter
- "Get on Google Play" opens the Play Store search for each game
- Installable on Android as an app (PWA) and works offline after the first load
- Light and dark theme follow the phone's setting

## Run it

No build step. Serve the folder with any static server:

```bash
cd Android_Games_Hub
python3 -m http.server 8080
# open http://localhost:8080
```

## Install on an Android phone

1. Host the folder on any HTTPS static host (GitHub Pages, Netlify, Vercel, Firebase Hosting).
2. Open the URL in Chrome on Android.
3. Tap the menu, then **Add to Home screen / Install app**.

To publish it on Google Play as a real APK/AAB, wrap the hosted PWA with
[Bubblewrap](https://github.com/GoogleChromeLabs/bubblewrap) (Trusted Web Activity):

```bash
npm i -g @bubblewrap/cli
bubblewrap init --manifest https://<your-host>/manifest.webmanifest
bubblewrap build
```

## Editing the game list

All games live in the `GAMES` array in `index.html`. Each entry looks like:

```js
{ name: "Shadow Fight 3", dev: "Nekki", cat: "fighting", rating: 4.5, downloads: 100,
  blurb: "Cinematic 3D martial arts...", tags: ["Story", "PvP duels"] }
```

- `cat`: one of `combat`, `survival`, `racing`, `fighting`, `inperson`
- `rating`: approximate Google Play star rating
- `downloads`: install count in millions (lower bound of the Play Store bracket, e.g. `100` = 100M+)

Ratings and download counts are approximate and change over time; check the store listing for current values.

## Files

```
Android_Games_Hub/
├── index.html            # the whole app (UI, styles, game data, logic)
├── manifest.webmanifest  # PWA manifest for "Add to Home screen"
├── sw.js                 # service worker for offline use
└── icons/                # app icons (SVG + 192/512 PNG)
```
