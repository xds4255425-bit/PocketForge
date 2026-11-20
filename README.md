# PocketForge — Web to APK

PocketForge is an open-source, Android-first website-to-APK project. It creates a standalone Android application shell that opens a website in Android WebView. APKs are built in GitHub Actions, so the owner can submit a build from a phone without installing Android Studio or an Android SDK locally.

**This release is an MVP foundation, not a claim of feature parity with every mature converter.** It focuses on a reproducible build, clear security boundaries, and a phone-friendly cloud workflow.

## What works in v0.2.0

- Enter a website URL, app name, Android package ID, and version.
- Trigger an Android APK build with GitHub Actions (`workflow_dispatch`).
- Build a separate APK artifact for each submitted configuration.
- Download the APK from the Actions run's Artifacts section.
- Android WebView shell with back navigation, loading indicator, safe browsing, no JavaScript bridge, no file/content access, and external handling for `mailto:`, `tel:`, and other non-web schemes.
- URL and package-name validation before build.
- CI validation tests for input rules.

## Phone-only workflow

1. Create a GitHub account and create a new repository from this project (or upload the ZIP contents).
2. In the repository, open **Actions** and enable workflows if GitHub asks.
3. Choose **PocketForge — Build APK**, tap **Run workflow**.
4. Enter:
   - `website_url`: full `https://` URL
   - `app_name`: visible Android app name
   - `application_id`: e.g. `com.example.myportal`
   - `version_name`: e.g. `1.0.0`
5. Start the workflow. When it finishes, open the run and download `pocketforge-apk`.
6. Extract the artifact ZIP on Android, tap the APK, and install. Android may ask permission to install from that source.

GitHub's UI wording can vary by app/browser version. The repository must be public for the simplest free Actions setup; private-repository minutes depend on the account plan.

## Build locally (optional)

Requirements: JDK 17, Android SDK platform 35, build-tools 35.0.0, Gradle 8.9+.

```bash
python scripts/validate_input.py \
  --url https://example.com \
  --app-name "My Portal" \
  --application-id com.example.myportal \
  --version-name 1.0.0

python scripts/generate_config.py \
  --url https://example.com \
  --app-name "My Portal" \
  --application-id com.example.myportal \
  --version-name 1.0.0

cd android
gradle --no-daemon assembleDebug
```

Output: `android/app/build/outputs/apk/debug/app-debug.apk`.

## Security and privacy

- The build runner does not download or crawl the target website. The URL is embedded into the app and fetched by the user's Android WebView at runtime.
- Only HTTPS URLs are accepted by default. Do not wrap websites you do not own or have permission to package.
- The generated app does not expose a JavaScript-to-native bridge.
- WebView file and content access are disabled; mixed content is blocked; Safe Browsing is enabled where supported.
- No user URL, source website content, or signing key is sent to a PocketForge-operated server. GitHub Actions processes the build in the repository owner's GitHub environment.
- This default debug APK is for testing/sideloading. For public distribution, configure your own release keystore in GitHub Actions Secrets and add a release-signing workflow. Never commit keystores or passwords.
- A WebView wrapper is not a full offline copy of a website. It still needs network access and the original site may restrict embedding, login, downloads, camera, microphone, payments, or other capabilities.

## Current limitations

- Android APK only; no iOS, Windows, or macOS outputs.
- URL wrapper only; local HTML/ZIP import, automatic favicon extraction, multi-tab navigation, custom splash/icon editor, permissions broker, push notifications, and app store release signing are not implemented yet.
- Some websites do not work correctly in Android System WebView. Web APIs differ from Chrome desktop and native apps.
- APK build is reproducible from source and workflow, but generated APKs are not yet reproducibly byte-identical because Android toolchain dependency versions can evolve.

## Roadmap

1. Local HTML/ZIP import with path traversal and archive-bomb protection.
2. Icon and splash-screen upload.
3. Multi-site bottom navigation and configurable theme.
4. Download/file chooser and camera/microphone permission mediation with explicit prompts.
5. Release signing with user-owned keystore secrets.
6. Build history and project recipe export/import.
7. Optional local build service with authentication, queue, quotas, and isolated workers.

## License

MIT. See `LICENSE`.
