# AGENTS.md – Repository‑wide Guidance for Automated Agents

---

## 1️⃣ Build, Lint & Test Commands

| Purpose | Gradle Command | Notes |
|--------|----------------|-------|
| **Clean** the project | `./gradlew clean` | Deletes `build/` directories. |
| **Assemble Debug APK** | `./gradlew assembleDebug` | Produces `app/build/outputs/apk/debug/app-debug.apk`. |
| **Assemble Release APK** | `./gradlew assembleRelease` | Requires signing config. |
| **Run all unit tests** | `./gradlew testDebugUnitTest` | Executes JVM‑based unit tests. |
| **Run a single unit test** | `./gradlew testDebugUnitTest --tests "com.example.package.ClassNameTest.methodName"` | Replace the fully‑qualified test name. |
| **Run all instrumented Android tests** | `./gradlew connectedDebugAndroidTest` | Runs on a connected device or emulator. |
| **Run a single Android test** | `./gradlew connectedDebugAndroidTest --tests "com.example.package.ClassNameTest#methodName"` |
| **Run lint (debug variant)** | `./gradlew lintDebug` | Generates `lint-results-debug.xml`. |
| **Auto‑fix lint where safe** | `./gradlew lintFixDebug` |
| **Run static analysis & checks** | `./gradlew check` | Executes lint, unit tests, and any custom checks. |
| **Run the full verification suite** | `./gradlew verify` *(alias for `check` on this project)* |
| **Generate dependency report** | `./gradlew dependencies` | Helpful for troubleshooting version conflicts. |

**Tip for CI pipelines** – combine the steps:
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

### 📚 Imports
1. **Order** (each group separated by a blank line):
   1. `android.*`
   2. `androidx.*`
   3. Third‑party libraries (e.g., `coil.*`, `kotlinx.*`, `io.ktor.*`).
   4. Project‑internal packages (e.g., `de.dkutzer.tcgwatcher.*`).
2. **No wildcard imports** – use explicit class names.
3. **Alphabetical within each group**.
4. **Static imports** (`import xyz.Foo.*`) only for constants or extension functions and placed after regular imports.

### 🧩 Formatting & Tools
- The project is configured for **Android Studio formatter**; run `./gradlew lintFixDebug` or use *Reformat Code* (⌥⌘L).
- **Ktlint** is *not* currently wired, but agents should still adhere to the official Kotlin style guide.
- Use `@Suppress("MagicNumber")` sparingly – prefer a `const val`.

### 🔤 Naming Conventions
| Element | Style |
|----------|-------|
| **Classes / Objects / Interfaces** | `PascalCase` (e.g., `SearchViewModel`) |
| **Functions / Properties / Variables** | `camelCase` (e.g., `loadCards()`, `isLoading`) |
| **Constants** | `UPPER_SNAKE_CASE` (e.g., `MAX_RETRY_COUNT`) |
| **Compose UI composables** | `PascalCase` ending with *`Screen`*, *`View`*, or *`Item`* (e.g., `SearchScreen`) |
| **Test classes** | Same as production class plus `Test` suffix (e.g., `SearchViewModelTest`) |
| **Test methods** | `functionName_state_expectedResult` (e.g., `search_success_returnsResults`) |

### 📊 Types & Nullability
- Prefer **non‑nullable** types. Use `?` only when a value can legitimately be absent.
- When exposing nullable values from a repository, wrap them in a **sealed `Result`** or `Either` to force explicit handling.
- Use **`typealias`** for complex generic signatures only when readability improves.
- Prefer **`val`** over `var` unless mutation is required.
- For collections, expose **immutable interfaces** (`List<T>`, `Set<T>`) and keep mutable implementations (`MutableList`) private.

### 🛡️ Error Handling & Logging
- **Checked exceptions** are rare in Kotlin – use `try/catch` only for I/O, network, or parsing that can legitimately fail.
- Convert low‑level exceptions into domain‑specific sealed classes (e.g., `SearchError.Network`, `SearchError.Parse`).
- **Logging**: Use `mu.KotlinLogging` (`logger.info { … }`). Do not leave `println` statements in production code.
- Never swallow exceptions without at least logging them.

### 🏛️ Architecture Guidelines
- **Repository pattern** – each feature has a `...Repository` interface and an implementation in `data/`.
- **DAO** classes reside in `data/*Dao.kt` and are annotated with Room annotations.
- **Domain models** live in `domain/` as plain Kotlin data classes. Keep them free of Android dependencies.
- **Use‑cases / interactors** – simple business‑logic functions placed in `domain/` or a dedicated `usecase/` package.
- **ViewModel** – `androidx.lifecycle.ViewModel` extending classes, expose UI state via `StateFlow` or `LiveData`.
- **Compose UI** – stateless composables receive all state via parameters; side‑effects are confined to `ViewModel`.

### 📱 Android Specific Rules
- **Resources** – strings, colors, dimensions must be defined in XML; avoid hard‑coded literals.
- **Permissions** – request at runtime only when needed; encapsulate logic in a `PermissionHandler`.
- **Coroutines** – UI‑related work on `Dispatchers.Main`; heavy IO on `Dispatchers.IO`. Scope: `viewModelScope`.
- **Dependency Injection** – project uses **KSP** for generated code (e.g., Hilt). Annotate with `@Inject` where appropriate.

### 🧪 Testing Guidelines
- **Unit tests** – pure Kotlin, no Android framework. Use **MockK** for mocking, **kotlinx.coroutines.test** for coroutine control.
- **Instrumentation tests** – use **AndroidX Test**, **Espresso**, **ComposeTestRule** for UI.
- **Arrange‑Act‑Assert** style; each test method should be short and focused.
- **Naming** – see the table above; include the scenario in the method name.
- **Run a single test** – agents should invoke the command listed in section 1.

---

## 3️⃣ Cursor / Copilot Rules (if present)
The repository currently **does not contain** a `.cursor/` directory nor a `.github/copilot‑instructions.md` file, so there are no explicit cursor or Copilot rules to obey.

If such files appear in the future, agents should:
1. Parse the rules verbatim.
2. Respect any *“never edit files matching …”* or *“always format with …”* directives.
3. Include the parsed rules in this `AGENTS.md` under a dedicated *Cursor / Copilot* section.

---

## 4️⃣ Frequently‑Used Scripts (optional helper)
```bash
# Quick sanity check – clean, build, run unit tests, lint
./gradlew clean assembleDebug testDebugUnitTest lintDebug

# Run a single test (replace with your fully‑qualified name)
./gradlew testDebugUnitTest --tests "de.dkutzer.tcgwatcher.collectables.search.data.MapperTest.testMapping"
```

---

*Generated for automated coding agents (Open‑Code, Copilot, Cursor, etc.) to ensure consistent builds, styling, and testing across the **TCGWatcher** codebase.*
