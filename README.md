# ImageForge

**Powerful Image Tools. Completely Offline.**

Native Android image-processing studio built with Kotlin and Jetpack Compose.

## Foundation

- Kotlin 2.2.10 / AGP 9.4.0 built-in Kotlin
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

## Phase status

### Phase 0 — Architecture & forensic specification

Contract documents and the Pi7 public capability/behavior specification are established. Pi7 implementation details are explicitly separated from verified public behavior.

### Phase 1 — Android foundation & core contracts

The native multi-module foundation, immutable processing models, typed results/errors, media/storage gateways, repository/use-case boundary, and baseline unit tests are established.

Feature processing is intentionally not implemented until the foundation verification remains green.
