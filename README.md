# Cyberterm for Android

Cyberterm is a GPU-rendered terminal for Android, built from the same Rust terminal engine as [Cyberterm desktop](https://github.com/cybercoretech/cyberterm). This first release starts a local `/system/bin/sh` in the app's private storage. It does not need root, an account, or network access.

## Install

Install from the [Cybercore F-Droid repository](https://cybercoretech.github.io/fdroid/) or download the APK from [Releases](https://github.com/cybercoretech/cyberterm-android-app/releases). Android will ask you to confirm installation of a downloaded APK.

Requirements: Android 8.0 (API 26) or newer; arm64-v8a or x86_64.

## Build

Install Android SDK platform 36, build-tools 36.0.0, NDK 30.0.16248370, Rust stable, and Rust targets `aarch64-linux-android` and `x86_64-linux-android`. Then:

```sh
./gradlew assembleDebug
```

Gradle builds the Rust NativeActivity library and packages it for both ABIs. For a connected device:

```sh
./gradlew installDebug
```

## First release scope

- Local Android shell (`/system/bin/sh`) only; the app does not bundle a Linux distribution or package manager.
- Terminal input supports the Android soft keyboard and physical keyboards. The soft keyboard can be shown from the app's keyboard control.
- Files live in Android's private app directory. There is no shared-storage or network permission.
- Desktop daemon, agent integrations, and multi-window controls are disabled on Android.

Report issues at [GitHub Issues](https://github.com/cybercoretech/cyberterm-android-app/issues). See [Cyberterm](https://cybercoretech.github.io/cyberterm/) for desktop builds and documentation.
