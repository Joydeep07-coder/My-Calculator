# My Calculator — Project Master Context & Update Log

**Document purpose:** This file is the project's long-term handoff/context document. Use it in future chats or AI Studio sessions to continue development without losing the project history, decisions, technical constraints, current status, or roadmap. Update it whenever meaningful changes are made.


## 1. Project identity

- **Project name:** My Calculator
- **App type:** Android multi-tool calculator
- **Primary platform:** Android
- **Technology:** Native Android, Kotlin, Jetpack Compose (not Flutter)
- **Development environment:** Google AI Studio Build/Code environment
- **Repository:** https://github.com/Joydeep07-coder/My-Calculator
- **Current development approach:** Work in AI Studio, review changes, then deliberately sync reviewed changes to GitHub. Do not assume automatic synchronization.
- **Developer constraint:** The project is being managed from an Android phone; instructions should be mobile-friendly, short, and step-by-step. Avoid requiring a desktop unless absolutely necessary.
- **No Firebase/authentication requirement:** Do not add Firebase, login, cloud accounts, or unrelated backend services unless explicitly requested.
- **Gemini/API keys:** The standard calculator features do not need a Gemini API key. Never add real API keys or secrets to source control or an APK.


## 2. Product goal

Build a polished, reliable, modern, feature-rich calculator app with a premium but minimalist interface. The calculator should be useful for ordinary calculations as well as scientific, financial, educational, conversion, graphing, and programming tasks. Correctness, usability, stability, privacy transparency, and maintainability take priority over adding more features.


## 3. Planned and implemented feature areas

The app's feature scope has included or planned:

1. Standard calculator / arithmetic and expression evaluation
2. Scientific calculator and trigonometric functions
3. Calculation history
4. GST calculator (including inclusive/exclusive calculations and tax presets/custom rates)
5. Finance calculator (EMI, interest, discount and related calculations)
6. Unit converter
7. Currency converter using a network exchange-rate service
8. Age calculator
9. BMI calculator
10. Programmer calculator (binary, octal, decimal, hexadecimal, bit operations)
11. Graph plotter
12. Settings, theme, angle mode, decimal precision, currency preference, haptic and sound preferences
13. Persistent calculation history and app settings
14. About and Privacy Policy information

**Feature scope should be verified against the actual current source code before calling any item complete.**


## 4. Technical foundation

- Android native application written in Kotlin.
- Jetpack Compose for UI.
- Gradle Kotlin DSL project files (`build.gradle.kts`, `settings.gradle.kts`).
- Android SDK target has previously been reported as API 36, with minimum API 24; verify actual Gradle configuration before release.
- The original project included a Gradle secrets plugin configuration referring to `.env.example` at the project root.
- The root `.env.example` file is required by the Gradle project configuration in the current setup. Missing this file causes Gradle project configuration to fail before tests run.
- Keep `.env.example` present at the root (same level as `settings.gradle.kts` and root `build.gradle.kts`). It must contain only safe placeholder/documentation content, no real credentials.
- No actual Gemini API integration was identified in the original project inspection; ordinary calculator functionality does not require an API key.
- The project should remain dependency-light; do not introduce dependencies without a clear need.


## 5. Important configuration safeguard: `.env.example`

A recurring failure has occurred because AI Studio removed or failed to preserve `.env.example`.

**Failure message:**

```text
The file '/app/applet/.env.example' could not be found
```

This occurs during Gradle project configuration because the Gradle secrets configuration expects `.env.example`. Tests do not start when this file is missing.

### Required safeguard
- Ensure `/app/applet/.env.example` exists before running Gradle.
- Do not remove it during cleanup, conflict resolution, or AI-assisted refactoring.
- Do not change the Gradle secrets configuration just to bypass the missing file without understanding the impact.
- Use placeholder documentation only; do not commit secrets.
- If missing, restore it with safe content, for example:

```text
# Example environment configuration
# No real secrets should be committed.
# Standard calculator functionality does not require a Gemini API key.
# If AI features are added later, configure credentials securely.
```

After restoring, verify that the file is in the exact root location, then rerun the intended command.


## 6. Development phases and reported results

The following results are based on reports from AI Studio/Gemini. They have not all been independently verified by inspecting the source or running the project outside AI Studio. Treat them as **reported**, not independently confirmed.

### Phase 1 — Core calculation engine
- Scope discussed: expression parsing, mathematical precedence, implicit multiplication, locale formatting, scientific functions, and test coverage.
- A complete final test report for Phase 1 is not available in this context.
- **Status:** Details and verification incomplete; verify from source/tests.

### Phase 2 — Age & BMI calculators
Reported work:
- Age calculator: date handling, UTC/local date considerations, leap years, month-end behavior, future-date validation, age calculations.
- BMI calculator: input validation, imperial conversion, healthy-weight range presentation, BMI categories.
- Reported test additions: `AgeAndBmiCalculatorUnitTest.kt` (10 tests) and `AgeAndBmiUiTest.kt` (2 UI tests).
- AI Studio reported the 12 added tests passed and Android build succeeded at that stage.
- **Status:** Reported successful; not independently verified.

### Phase 3 — Programmer calculator
Reported work:
- Binary, octal, decimal, hexadecimal representation.
- Arithmetic and bitwise operations (AND, OR, XOR, NOT, shifts).
- Signed/unsigned display, two's-complement negative formatting, word sizes (8/16/32/64 bits), overflow behavior, validation/error handling.
- Copy-on-tap and haptic integration.
- Reported tests: `ProgrammerCalculatorUnitTest.kt` (7 tests) and `ProgrammerCalculatorUiTest.kt` (2 UI tests).
- AI Studio reported 32 total tests passing and Android build success at that stage.
- **Status:** Reported successful; edge cases still deserve audit.

### Phase 4 — Graph plotter
Reported work:
- Expression compilation, expression validation, function/angle-mode integration.
- Pole/discontinuity handling, polyline splitting, clipping, pan/zoom, tick formatting, axis labels and viewport label.
- Compiled-expression caching.
- Reported tests: `GraphPlotterUnitTest.kt` (8 tests) and `GraphPlotterUiTest.kt` (1 UI test).
- AI Studio reported 41 tests passing and Android build success at that stage.
- Potential audit concerns: discontinuity detection may miss some cases; supported AST functions; graph edge cases.
- **Status:** Reported successful; not independently verified.

### Phase 5 — GST, Finance & Converters
Reported work:
- Settings currency preference (default INR), currency formatting helpers and selector.
- GST: inclusive/exclusive calculation, rate presets/custom rate, taxable amount, total GST, CGST/SGST/IGST and final payable; validation.
- Finance: EMI, simple/compound interest and discount helpers, validation, zero-interest handling and monthly summary.
- Unit converter: precision refinements, conversion constants, validation, category navigation/scroll and layout adjustments.
- Currency converter: exchange-rate service and network permission; API endpoint reported as `https://open.er-api.com/v6/latest/USD`; JSON parsing; loading, success, error/retry states; timestamp/source reporting; no fake live rates.
- Reported additions include `ExchangeRateService.kt` and three test files; modifications to app models, ViewModel, settings/about, catalogs, GST/finance/converter screens, navigation, and manifest.
- AI Studio reported 58 tests passing and Android build success at that stage.
- Known audit items: cross-currency formula must be correct (cross-rate should be amount × target rate ÷ source rate when rates are quoted against a common base); validate rate codes and timestamps; network handling/timeouts/error states; exchange-rate provider terms, attribution and rate limitations; never present rates as real-time without an accurate update timestamp.
- **Status:** Reported successful; not independently verified.

### Phase 6 — History & settings persistence
Reported work:
- Persistent calculation history (Room/SQLite reported), with expression, result, timestamp, calculation/tool type.
- Restore history across app restarts.
- Duplicate/invalid history rejection.
- Individual history deletion and Clear All with confirmation; search/filter; reuse calculation.
- Settings persistence via DataStore Preferences reported, including theme, currency, decimal precision, angle mode, haptic, sound, and reset to defaults.
- Privacy/About content describing local storage and network behavior.
- Reported tests: `Phase6PersistenceUnitTest` (4 tests) and `Phase6UiTest` (4 UI tests after fixes).
- `Phase6PersistenceUnitTest`: reported 4/4 passed.
- Initial UI tests: 2 passed, 2 failed. Failures were test issues: search semantics found both search field and history card; Privacy Policy sheet was only partially expanded and network-provider text was below the fold.
- Fixes were reported in `Phase6UiTest.kt`: disambiguated the history search matcher and waited for reset Snackbar to disappear before tapping Privacy Policy. Production UI was not changed for these test failures.
- Latest Phase 6 UI report: **4/4 passed**, 0 failed, 0 skipped, 0 errors; Gradle `BUILD SUCCESSFUL` in 38 seconds.
- **Status:** Persistence tests reported passed (4/4); UI tests reported passed (4/4). These tests were not independently verified.


## 7. Launcher icon / branding

- User selected concept **Design #01** from a 40-concept logo sheet.
- Design direction: dark navy rounded-square background, four white calculator keys with +, −, ×, ÷, and a small blue analytics badge with ascending bars.
- Intended style: flat/minimal, crisp at small sizes, balanced padding, blue badge kept proportionally small, no text.
- AI Studio was instructed to implement launcher icon assets (adaptive foreground/background, legacy icons), update manifest references as needed, preserve UI and app functionality, avoid new dependencies, and build-check.
- User later stated the logo change was done.
- **Status:** User-reported completed; files and build result not yet inspected in this context. Verify icon assets, adaptive safe zone, manifest, and build.


## 8. GitHub repository and sync status

Repository: https://github.com/Joydeep07-coder/My-Calculator

GitHub setup previously completed using Termux on Android:
- Git installed and configured.
- Repository initialized and remote added.
- GitHub CLI installed and authenticated to account `Joydeep07-coder`.
- Initial push conflict due to remote README history was resolved by pulling with unrelated histories and merging README files.
- Successfully pushed `main` and set tracking to `origin/main`.
- A `.env.example` placeholder was restored through GitHub Codespaces at one point.
- Earlier Git status was reported clean and synchronized, but that was before later AI Studio phases.

**Current sync status:** Unknown. AI Studio changes from Phases 2–6 and launcher icon updates have not been confirmed pushed to GitHub. Do not assume sync is automatic.

### Safe sync procedure
1. Check AI Studio GitHub/sync panel for pending changes.
2. Review changed files and diff if available.
3. Confirm `.env.example` is present at project root.
4. Confirm no API keys, `.env` files with secrets, keystores, passwords, or signing material are included.
5. Sync/push the reviewed changes to GitHub.
6. Open GitHub and verify latest commit and changed files.
7. Do not force push; do not upload the old ZIP over the updated project.
8. Preserve repository history.


## 9. Gradle/test runner issues and history

### Gradle daemon hang
- A prior `gradle :app:testDebugUnitTest` rerun hung before first task output (`Last progress: never`).
- Gradle daemon PID 63 was BUSY; PID 4147 was IDLE.
- `gradle --stop` completed with exit code 0 and stopped two daemons.
- `gradle --status` then reported no Gradle daemons running.
- Running test command with `--no-daemon` allowed Phase 6 UI tests to complete successfully.
- Prefer `--no-daemon` for troubleshooting in this AI Studio environment; if a run hangs, stop it and report rather than automatically retrying.

### Missing `.env.example`
- The complete suite previously failed before running tests because `.env.example` was absent.
- Error was during project configuration: `Failed to notify project evaluation listener` and `The file '/app/applet/.env.example' could not be found`.
- The file was restored; user confirmed the error was fixed.
- Complete test suite must now be rerun with `.env.example` present.

### Latest complete-suite status
- The most recent attempt at the full suite stopped during project configuration due to the missing `.env.example` file; 0 tests executed.
- After restoring `.env.example`, user indicated the issue was fixed and a complete test run was requested, but no final result has been supplied in this context.
- **Status:** Full suite pending.


## 10. Current state snapshot (as of 2026-11-20)

### Reported completed
- Native Android Kotlin + Jetpack Compose project.
- Core calculator and multiple advanced calculators/features have been implemented across Phases 2–6 (details above; reports only).
- Phase 6 persistence unit tests reported passing 4/4.
- Phase 6 UI tests reported passing 4/4 after test fixes.
- Launcher icon change reported completed.
- GitHub repository exists and initial project history was pushed.
- `.env.example` restored after a missing-file build/test configuration issue.

### Not yet verified / pending
- Complete unit test suite after restoring `.env.example`.
- Final Android build after all phases and icon update.
- Independent source/code review or clean repository build.
- Actual app install/run testing on a physical Android device.
- UI audit across small/large screens, dark/light themes and edge cases.
- Current AI Studio changes synced to GitHub.
- Release version/package ID review, signing configuration, release APK/AAB and Play Store readiness.


## 11. Immediate next steps (recommended sequence)

1. **Run the complete unit test suite** with the missing `.env.example` restored:
   ```bash
   gradle :app:testDebugUnitTest --no-daemon
   ```
   Do not modify source during this verification. Wait for completion and report counts and failures.
2. **Review/sync to GitHub:** inspect pending changes and diff, confirm secrets are excluded and `.env.example` included, then sync. Verify the commit on GitHub.
3. **Final Android build verification:** run the supported build task in AI Studio (or the environment's build action) after tests. Report the exact result.
4. **Full app audit:** inspect all calculators and edge cases, navigation, history, settings persistence, currency service behavior, error handling, privacy statements, accessibility/semantics and responsive layouts.
5. **Physical device test:** install the generated build and exercise core flows.
6. **Release preparation:** verify package ID, versionCode/versionName, signing, launcher icons, store assets, permissions, privacy policy and release bundle.

Avoid adding new features until the test/build/audit steps are complete.


## 12. Known risks and audit checklist

### Calculation correctness
- Expression precedence and associativity.
- Implicit multiplication behavior and ambiguous input.
- Division by zero and invalid expressions.
- Scientific functions, trig angle mode (DEG/RAD), logarithms, powers, roots, factorial and domain errors.
- Floating-point rounding/display precision.
- Negative values and repeated operators.
- Programmer calculator signed/unsigned boundaries, overflow, 64-bit handling and shifts.
- Graph plotter discontinuities, clipping, scaling, pan/zoom, extreme inputs and unsupported expressions.
- Age calculator leap-day and month-end cases, future dates and time-zone boundaries.
- BMI conversion accuracy, category thresholds and invalid inputs.
- GST inclusive/exclusive math, rate boundaries, custom rates, CGST/SGST/IGST handling and rounding.
- EMI/interest/discount edge cases, zero interest, zero term and invalid principal.
- Unit conversions: constants, small/large values, precision and category correctness.
- Currency conversion: correct cross-rate formula, network failures, rate freshness, source attribution, parsing and invalid currency handling.

### Persistence and state
- History persistence across restart.
- Settings persistence across restart.
- Delete one history item, Clear All, confirmation/cancel, search and reuse.
- Reset settings correctly restores all defaults.
- Data migrations and Room schema changes.
- Backup/restore behavior should be intentionally configured and tested if desired; do not claim backup is implemented unless verified.

### UI/UX
- Adaptive launcher icon appears correctly on Android launchers.
- Button sizing, contrast, typography and spacing.
- Modal sheet expansion, scrolling and snackbar overlap.
- Keyboard/input handling and accessibility semantics.
- Dark/light theme consistency.
- Small screen, large screen, landscape and system font scaling.
- Empty states, loading states, error states and retry controls.
- Touch targets and visual clipping.

### Security/privacy
- No embedded API keys, secrets, signing files or credentials in GitHub.
- INTERNET permission is only used where needed and disclosed.
- Privacy/About text accurately reflects local storage and network use.
- No false claim that currency data is real-time if provider updates periodically.
- No unnecessary permissions.


## 13. Working rules for future AI sessions

When continuing this project, use these rules:

1. Read this document first and use it as project context.
2. Do not switch the project to Flutter; it is Kotlin + Jetpack Compose.
3. Do not reset, overwrite, or replace the repository/project with the original ZIP.
4. Do not remove `.env.example`.
5. Do not add unrelated features while verifying a phase.
6. Make one scoped change at a time; report files changed, tests run, and exact result.
7. Distinguish reported test results from independently verified results.
8. Do not claim a build or tests passed without an actual result.
9. Keep secrets out of source code, GitHub and APK.
10. Before syncing, review changes and ensure the GitHub repository is up to date without force-pushing.
11. Prefer commands with `--no-daemon` when the AI Studio Gradle environment has daemon issues.
12. When a command hangs, stop it, inspect status/logs, and do not retry automatically.
13. User is working on Android phone; provide short, sequential steps and wait for screenshots/results when needed.
14. Do not assume AI Studio automatically syncs changes to GitHub.
15. Preserve existing UI and features unless the user explicitly asks for a change.
16. Use concise responses; avoid unsolicited roadmap details when the user asks for a single next action.


## 14. Project status table

| Area | Status | Confidence |
|---|---|---|
| Android Kotlin/Compose project | Established | High (project context) |
| Repository created and initial push | Done | Reported |
| Standard calculator/core engine | Implemented/planned in project | Phase 1 details incomplete |
| Age & BMI | Implemented | AI Studio reported tests/build pass |
| Programmer calculator | Implemented | AI Studio reported tests/build pass |
| Graph plotter | Implemented | AI Studio reported tests/build pass |
| GST/Finance/Unit/Currency converters | Implemented | AI Studio reported tests/build pass |
| History persistence | Implemented | AI Studio reported 4/4 persistence tests pass |
| Settings persistence | Implemented | AI Studio reported tests pass |
| Phase 6 UI tests | Passing | AI Studio reported 4/4 pass |
| Full unit suite | Pending | No final result provided after restoring `.env.example` |
| Launcher icon | Changed | User reported done; not inspected |
| Final Android build | Pending | No final build result after all changes |
| GitHub sync of latest changes | Pending | Must check AI Studio sync panel and verify commit |
| Device installation/test | Pending | Not confirmed |
| Release signing / AAB | Pending | Not completed/confirmed |


## 15. Useful project paths and repository

- GitHub repo: `https://github.com/Joydeep07-coder/My-Calculator`
- Expected root configuration: `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`, `.env.example`
- App module: `app/`
- Android source: `app/src/main/`
- Unit tests: `app/src/test/`
- Instrumented/UI tests: `app/src/androidTest/`
- History/persistence likely uses Room/SQLite; verify actual files.
- Settings persistence reported to use DataStore Preferences; verify actual files.
- Currency network service reported in `ExchangeRateService.kt`; verify actual file and provider.


## 16. Change log template

Add a new entry whenever changes are made:


### [Date] — [Phase/Change]
- **Goal:**
- **Files changed:**
- **Implementation details:**
- **Tests run:**
- **Test result:**
- **Build result:**
- **GitHub sync/commit:**
- **Known issues / follow-up:**


## 17. Handoff prompt for a future ChatGPT conversation

Copy this when starting a new chat and attach/paste this project document:


> Continue my My Calculator Android app project using this project master context. First, read the full document. Treat AI Studio-reported results as reported rather than independently verified. The project is native Android Kotlin + Jetpack Compose, not Flutter. Do not overwrite/reset the project, do not remove `.env.example`, do not add real secrets, and do not add unrelated features. My immediate next task is to confirm the complete unit test suite after restoring `.env.example`, then review and sync the current AI Studio changes to GitHub, verify the Android build, and audit the app. Give me one mobile-friendly step at a time and wait for my result/screenshot.


---

**Last updated:** 2026-11-20
**Current immediate task:** Complete unit test suite result is pending after `.env.example` restoration.
