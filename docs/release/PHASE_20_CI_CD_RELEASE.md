# Phase 20 — CI/CD & Release

## Scope

Phase 20 hardens the production pipeline so every main-branch release build is checked before artifacts are published.

### CI gates

1. **Lint** — runs the application release lint task.
2. **Unit tests** — runs the existing core test suites.
3. **Offline/privacy guardrails** — blocks network permissions and common network-client usage.
4. **Release compilation** — builds the release variants and required release modules.
5. **Split APK validation** — requires exactly four architecture-specific APKs: arm64-v8a, armeabi-v7a, x86_64, and x86.
6. **APK signature verification** — each release APK is verified with `apksigner`.
7. **Release smoke validation** — checks application id `org.techwithkaushik.imageforge`, version code `1`, version name `0.1.0`, expected native ABI, and valid APK signature.
8. **Release-size reporting** — the Phase 18 APK-size report remains an uploaded artifact.
9. **Individual artifacts** — each architecture APK is uploaded separately; no combined ZIP is used.

## Signing

Release signing is configured only when all four environment values are present: `SIGN_KEY_STORE`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, and `SIGNING_STORE_PASSWORD`.
GitHub Actions decodes the repository signing keystore into the runner and passes the signing values only to the release build step. Secrets are never committed.
A release artifact that cannot pass `apksigner verify --verbose` is rejected by CI.

## Instrumentation status

The repository currently has no `src/androidTest` test source sets. Phase 20 therefore does not add an emulator-dependent instrumentation job. The automated smoke gate validates generated release APKs directly. Device-level functional smoke testing remains a final release-owner check on a physical Android device.

## Release artifact policy

Artifacts are architecture-specific and independently downloadable from the workflow run. The universal APK remains disabled to keep release packages lean.

Before publishing a release: CI green; all four APK artifacts present; signatures verified; package/version/ABI smoke checks passed; Phase 18 size report available; final device smoke test passed; offline/privacy behavior validated.

## Reproducibility

The workflow pins Java 17, Gradle 9.6.0, and OpenCV Android SDK 5.0.0. Gradle and OpenCV caching are enabled for repeat builds.

## Out of scope

Phase 20 does not publish directly to Google Play and does not introduce an emulator farm. It establishes a deterministic GitHub Actions release-validation pipeline and production-ready downloadable artifacts.
