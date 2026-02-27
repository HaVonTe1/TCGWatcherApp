# AGENTS.md – Repository‑wide Guidance for Automated Agents

---

## 1️⃣ Build, Lint & Test Commands

| Purpose | Gradle Command | Notes |
|--------|----------------|-------|
| **Clean** the project | `./gradlew clean` | Deletes all `build/` directories. |
| **Assemble Debug APK** | `./gradlew assembleDebug` | Produces `app/build/outputs/apk/debug/app-debug.apk`. |
| **Assemble Release APK** | `./gradlew assembleRelease` | Requires a signing configuration. |
| **Run all unit tests** | `./gradlew testDebugUnitTest` | JVM‑based unit tests only. |
| **Run a single unit test** | `./gradlew testDebugUnitTest --tests "<fully‑qualified‑class>.testMethod"` | Replace with the exact class and method name. |
| **Run all instrumented Android tests** | `./gradlew connectedDebugAndroidTest` | Executes on a connected device or emulator. |
| **Run a single Android test** | `./gradlew connectedDebugAndroidTest --tests "<fully‑qualified‑class>#testMethod"` | Use `#` for the method separator. |
| **Run lint (debug variant)** | `./gradlew lintDebug` | Generates `lint-results-debug.xml`. |
| **Auto‑fix lint where safe** | `./gradlew lintFixDebug` | Applies automatic fixes; review afterward. |
| **Run static analysis & checks** | `./gradlew check` | Executes lint, unit tests, and any custom checks. |
| **Run the full verification suite** | `./gradlew verify` *(alias for `check`)* | Ideal for CI pipelines. |
| **Generate dependency report** | `./gradlew dependencies` | Helpful for troubleshooting version conflicts. |
| **Run tests with verbose output** | `./gradlew testDebugUnitTest --info` | Shows detailed test execution logs. |
| **Run a single test with debug logging** | `./gradlew testDebugUnitTest --tests "..." --debug` | Enables Gradle debug output. |
| **Run instrumentation tests on a specific device** | `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.deviceSerial=<serial>` | Useful for multi‑device farms. |

**Tip for CI pipelines** – combine the most common steps:
```bash
./gradlew clean assembleDebug lintDebug testDebugUnitTest connectedDebugAndroidTest
```

---

## 2️⃣ Code‑Style Guidelines

### 📦 General Kotlin Conventions
- **Indentation:** 4 spaces (no tabs).  
- **Line length:** ≤ 100 characters.  
- **Trailing whitespace:** Never commit.  
- **File name:** Must match the primary class / object name (`FooBar.kt`).  
- **Package declaration:** First line, all lower‑case, mirroring folder hierarchy.  
- **Kotlin version:** 2.3.0 (declare `kotlin.code.style=official` in `gradle.properties`).  
- **Language features:** Prefer `val` over `var`; use sealed classes, data classes, and inline classes where appropriate.

### 📚 Imports
1. **Order** (blank line between groups):
   1. `android.*`
   2. `androidx.*`
   3. Third‑party libraries (e.g., `coil.*`, `kotlinx.*`, `io.ktor.*`, `io.github.oshai.*`).
   4. Project‑internal packages (e.g., `de.dkutzer.tcgwatcher.*`).
2. **No wildcard imports** – always import explicit classes/functions.  
3. **Alphabetical** within each group.  
4. **Static imports** (`import xyz.Foo.*`) only for constants or extension functions; place after regular imports.
5. **IDE assistance:** enable *Optimize Imports on Save* in Android Studio.

### 🧩 Formatting & Tools
- **Android Studio formatter** is the source of truth – run `./gradlew lintFixDebug` or press **⌥⌘L**.  
- **Ktlint** is not wired, but agents should still adhere to the official Kotlin style guide.  
- **Suppressions:** Use `@Suppress("MagicNumber")` sparingly; extract magic numbers to `const val`.  
- **Logging:** Use `kotlin-logging-jvm` (v7.0.14) via `mu.KotlinLogging`. Example:
  ```kotlin
  private val logger = KotlinLogging.logger {}
  logger.info { "Fetching cards" }
  ```
- **Documentation:** Write KDoc for public API (classes, functions, properties). Include `@param`, `@return`, and `@throws` where relevant.

### 🔤 Naming Conventions
| Element | Style |
|----------|-------|
| **Classes / Objects / Interfaces** | `PascalCase` (e.g., `SearchViewModel`) |
| **Functions / Properties / Variables** | `camelCase` (e.g., `loadCards()`, `isLoading`) |
| **Constants** | `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`) |
| **Compose UI composables** | `PascalCase` ending with `Screen`, `View`, or `Item` (e.g., `SearchScreen`) |
| **Test classes** | Same as production class + `Test` suffix (e.g., `SearchViewModelTest`) |
| **Test methods** | `functionName_state_expectedResult` (e.g., `search_success_returnsResults`) |
| **Extension functions** | `camelCase` prefixed with the type they extend (e.g., `String.toCardId()`). |

### 📊 Types & Nullability
- Prefer **non‑nullable** types. Use `?` only when a value can legitimately be absent.  
- When exposing nullable data from repositories, wrap them in a **sealed `Result`** or `Either` to force explicit handling.  
- **Type aliases** (`typealias`) are allowed for complex generic signatures when readability improves.  
- **Collections:** Expose immutable interfaces (`List<T>`, `Set<T>`) publicly; keep mutable implementations (`MutableList`, `MutableSet`) private.  
- **Coroutines:** Use `suspend` functions for asynchronous work; expose `Flow<T>` or `StateFlow<T>` for streams.

### 🛡️ Error Handling & Logging
- **Exceptions:** Kotlin has unchecked exceptions; catch only when you can recover or add context.  
- Convert low‑level exceptions into domain‑specific sealed classes (e.g., `SearchError.Network`, `SearchError.Parse`).  
- **Logging:** Use `mu.KotlinLogging`. Never leave `println` statements in production code.  
- **Fail fast:** Validate arguments at function entry (`require`, `check`).  
- **Never swallow exceptions** without at least logging them.

### 🏛️ Architecture Guidelines
- **Repository pattern:** Each feature has a `...Repository` interface and an implementation in `data/`.  
- **DAO layer:** Classes in `data/*Dao.kt` annotated with Room; keep SQL confined to DAO.  
- **Domain models:** Plain Kotlin data classes in `domain/`; free of Android dependencies.  
- **Use‑cases / interactors:** Simple business‑logic functions placed in `domain/` or a dedicated `usecase/` package.  
- **ViewModel:** Extend `androidx.lifecycle.ViewModel`; expose UI state via `StateFlow` or `LiveData`.  
- **Compose UI:** Stateless composables receive state via parameters; side‑effects are confined to `ViewModel` or `LaunchedEffect`.  
- **Dependency Injection:** Project uses **Hilt/KSP**. Annotate constructors with `@Inject` and provide modules in `di/`.

### 📱 Android‑Specific Rules
- **Resources:** Strings, colors, dimensions must be defined in XML; avoid hard‑coded literals.  
- **Permissions:** Request at runtime only when needed; encapsulate logic in a `PermissionHandler`.  
- **Coroutines:** UI work on `Dispatchers.Main`; heavy I/O on `Dispatchers.IO`. Scope: `viewModelScope` for ViewModels, `lifecycleScope` for Activities/Fragments.  
- **Compose previews:** Add `@Preview` for debug builds; keep them lightweight.
- **ProGuard / R8:** Keep rules in `proguard-rules.pro`; avoid reflection without explicit keep rules.

### 🧪 Testing Guidelines
- **Unit tests:** Pure Kotlin, no Android framework. Use **MockK** for mocking and **kotlinx.coroutines.test** for coroutine control.  
- **Instrumentation tests:** Use **AndroidX Test**, **Espresso**, **ComposeTestRule** for UI.  
- **Arrange‑Act‑Assert** pattern; each test method should be short and focused.  
- **Naming:** Follow the table above; include scenario and expected outcome.  
- **Test fixtures:** Keep them in `src/test/resources` or `src/androidTest/resources`.  
- **Running a single test:** Use the command from section 1.  
- **Coverage:** Run `./gradlew jacocoTestReport` to generate coverage reports.
- **Test dependencies:** `junit:4.13.2`, `mockk:1.14.9`, `kotlinx-coroutines-test:1.10.2`, `robolectric:4.16.1`.

---

## 3️⃣ Cursor / Copilot Rules (if present)
The repository currently **does not contain** a `.cursor/` directory nor a `.github/copilot‑instructions.md` file, so there are no explicit cursor or Copilot rules to obey.

If such files appear in the future, agents should:
1. Parse the rules verbatim.
2. Respect any *"never edit files matching …"* or *"always format with …"* directives.
3. Include the parsed rules in this `AGENTS.md` under a dedicated *Cursor / Copilot* section.

---

## 4️⃣ Frequently‑Used Scripts (optional helper)
```bash
# Quick sanity check – clean, build, run unit tests, lint
./gradlew clean assembleDebug testDebugUnitTest lintDebug

# Run a single unit test (replace with fully‑qualified name)
./gradlew testDebugUnitTest --tests "de.dkutzer.tcgwatcher.collectables.search.data.MapperTest.testMapping"

# Run a single Android test on a specific device
./gradlew connectedDebugAndroidTest --tests "de.dkutzer.tcgwatcher.ui.CardListTest#testCardDisplays" -Pandroid.testInstrumentationRunnerArguments.deviceSerial=abcd1234

# Generate a dependency report useful for troubleshooting
./gradlew dependencies > deps.txt
```

---

*Generated for automated coding agents (Open‑Code, Copilot, Cursor, etc.) to ensure consistent builds, styling, and testing across the **TCGWatcher** codebase.*
