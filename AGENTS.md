# Sufara project guide

## Architecture

- This is a Kotlin/Android app using Jetpack Compose, Navigation Compose, Room, and local assets. Java 17, compile/target SDK 34, and min SDK 24 are configured in the Gradle modules.
- `app` is the composition root. It creates the Room database and lesson repository, owns navigation and the main menu, and packages lesson content from `app/src/main/assets/lekcije`.
- `core:designsystem` contains the theme, palette, typography, shared Compose controls, and the app-wide geometric background.
- `feature:lesson` contains lesson domain models/repository contracts, Room entities/DAO, asset parsing, persisted settings, the unified 2D reading/writing course map, viewers, quizzes, Anki-style review, text processing, Arabic renderers, and the path-based handwriting matcher.
- The course map uses cached deterministic four-region geometry with arc-length lesson placement; camera pan/zoom state is separate from world geometry. Writing displays and scores the same fixed canonical guide coordinates.
- `core:common`, `core:database`, `core:network`, `feature:map`, and `feature:spectrogram` are currently placeholder modules with empty build scripts.

## Content conventions

- Lesson folders use the `NNN Title` naming scheme and are sorted by their zero-padded numeric prefix. Prefixes must be unique but need not be consecutive: the app assigns a zero-based `ordinal` after sorting while keeping the source prefix as the stable Room/navigation ID.
- `симболи.md` has one non-blank symbol line per sorted lesson folder. A `.` denotes the book fallback symbol.
- A lesson may contain `лекција.md`, `додатак.md`, `квиз.md`, `примери.md`, and `исходиште.png`.
- Recorded examples live under `app/src/main/assets/lekcije/audio/<source-prefix>`. MP3 names are paired with non-blank `примери.md` lines in plain lexicographic order. Count mismatches must be logged and handled with nullable audio, never by crashing or shifting later recordings.
- Writing lessons are derived only from non-blank lines in `примери.md`; generated reading examples are explicitly excluded and writing progress is stored separately from reading progress.
- The traditional 17-point articulation map lives in `MakharijCatalog`; packaged reference SVGs are under `app/src/main/assets/makharij` and every imported asset must retain verified provenance in `LICENSES.md`.
- Quiz questions are unindented numbered lines; answers are indented numbered lines. The first answer is the correct answer and answer order is shuffled in the UI.
- Do not rewrite, complete, or remove lesson/fact content unless the user explicitly requests content changes. Preserve Cyrillic and Arabic filenames and text encoding.

## Build and validation

- Point `JAVA_HOME` to a JDK 17 installation.
- On Windows use `./gradlew.bat test lint assembleDebug`; on macOS/Linux use `./gradlew test lint assembleDebug`.
- For a focused lesson-module check use `./gradlew.bat :feature:lesson:testDebugUnitTest` (or `./gradlew` on macOS/Linux).
- After every change, inspect the complete git diff and run the relevant unit tests plus compile/lint/assemble checks. Fix safe, in-scope regressions before handing work off.

## Change rules

- Keep UI changes incremental and consistent with the dark-blue, parchment, and gold visual system. Preserve existing screen flows unless a behavior is demonstrably broken.
- Keep visible copy concise and factual. Do not use em dashes in application text or project documentation.
- The application is intentionally portrait-only. Do not re-enable landscape layouts unless the user explicitly changes that product decision.
- Make layouts work on narrow and short mobile screens: prefer scrolling/wrapping containers, bounded widths, and density-independent gesture thresholds. Interactive elements must not overlap or become unreachable.
- Keep the geometric background decorative, low contrast, non-interactive, and shared at the app root. Cache static drawing geometry and avoid adding per-item infinite animations when only one visible element needs motion.
- Keep blocking asset/database work off the main thread. Static asset parsing may be cached; progress and review state must continue to come from Room.
- Release media playback when an example card changes or leaves composition. Writing guide caches must remain bounded because the course contains many examples.
- Settings that are user-facing must remain persisted through `SharedPreferences`; reading, writing, and quiz progress must remain persisted through Room with non-destructive migrations.
