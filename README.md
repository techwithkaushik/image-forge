# ImageForge

**Powerful Image Tools. Completely Offline.**

Native Android image-processing studio built with Kotlin and Jetpack Compose.

## Foundation

- Kotlin 2.4.20
- Android Gradle Plugin 9.4.0
- Gradle 9.6
- Jetpack Compose BOM 2026.09.00
- Clean Architecture + MVVM
- StateFlow + Coroutines
- Multi-module
- Native Android image APIs first
- Selective/minimal OpenCV only where justified
- Offline-first core processing

## Modules

- :app
- :core:common
- :core:designsystem
- :core:imageprocessor
- :core:media
- :core:storage
- :core:ocr
- :feature:dashboard
- :feature:editor
- :feature:documents

## Architecture

Compose Screen -> ViewModel -> StateFlow -> UseCase -> Repository -> ImageProcessor -> Android APIs / selective OpenCV -> Storage

Feature modules may depend on core modules. Core modules never depend on feature modules.

## Status

Phase 0 architecture contracts and the initial native Android module skeleton are being established. Feature processing is intentionally not implemented yet.
