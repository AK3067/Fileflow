## FileFlow

FileFlow is a **gallery-style media + office suite** experience (web app + Android wrapper).

## Web (React + Vite)

```bash
npm install
npm run dev
```

## Android (Capacitor)

- **Android project folder**: `Android/`
- **Compatibility note**: there is also an `android` symlink pointing to `Android/` so Capacitor CLI commands keep working.

### Prerequisites

- Android Studio + Android SDK installed

### Build + sync web into Android

```bash
npm run android:sync
```

### Open in Android Studio

```bash
npm run android:open
```

### Update Android launcher icon (uses your logo)

1. Put your logo at `public/logo.png`
2. Generate Android icons + splash:

```bash
npx capacitor-assets generate --android --assetPath public
```
