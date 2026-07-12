# Riot4K Samples

Every sample is a complete, standalone consumer of the SDK with a layered
architecture (an `AccountRepository` mapping SDK results onto app-level states)
and its own test suite. They come in two flavors:

- **`platforms/`** — the *same Kotlin consumer on every platform the SDK ships
  to*. These consume the SDK from source via the included build and prove the
  shared-code promise: identical logic on the JVM, Android, Node.js, Linux, and
  iOS.
- **`languages/`** — *other languages consuming the SDK's real distribution
  artifacts*, exactly like an external user: Java via the Maven JVM artifact,
  TypeScript via the npm package, Swift via the XCFramework.

Run everything from the repository root. Live runs read the API key from the
`RIOT_API_KEY` environment variable — no key is ever committed.

## Platform samples (Kotlin everywhere)

| Sample | Platform | Run live | Run tests |
| ------ | -------- | -------- | --------- |
| `platforms/jvm` | JVM CLI | `RIOT_API_KEY=... ./gradlew -p samples :jvm:run --args="gameName tagLine"` | `./gradlew -p samples :jvm:test` |
| `platforms/android` | Android app | `RIOT_API_KEY=... ./gradlew -p samples :android:installDebug` | `./gradlew -p samples :android:testDebugUnitTest` |
| `platforms/nodejs` | Node.js | `RIOT_API_KEY=... ./gradlew -p samples :nodejs:jsNodeRun` | `./gradlew -p samples :nodejs:jsTest` |
| `platforms/linux` | Linux executable | build then run the `.kexe` (see below) | `./gradlew -p samples :linux:linuxX64Test` |
| `platforms/ios` | iOS framework | consumed from Xcode | compile-checked in CI |

## Language samples (the SDK from other languages)

| Sample | Language | Run live | Run tests |
| ------ | -------- | -------- | --------- |
| `languages/java` | Java 21 | `RIOT_API_KEY=... ./gradlew -p samples :java:run` | `./gradlew -p samples :java:test` |
| `languages/typescript` | TypeScript / Node | `RIOT_API_KEY=... npm run live` (in `samples/languages/typescript`, after `npm install`) | `npm test` (see below) |
| `languages/swift` | Swift (SPM + XCTest) | — | `samples/languages/swift/run-tests.sh` (macOS) |

## Contract tests and the mock Riot server

Test suites run against a local stand-in for the Riot API so every language
binding provably maps the same responses to the same states. The behaviors and
fixtures are defined in [`fixtures/README.md`](fixtures/README.md).

- JVM-hosted suites (`platforms/jvm`, `platforms/android`, `languages/java`)
  embed the server in-process; nothing to start manually.
- `languages/typescript`'s vitest setup spawns it automatically (build it once
  first with `./gradlew -p samples :mock-riot-server:installDist`, and build the
  JS distribution with `./gradlew :riot4k-api:jsNodeProductionLibraryDistribution`
  before `npm install`).
- `languages/swift` expects the server's port in `MOCK_RIOT_PORT`; use
  `scripts/mock-server.sh start` / `stop`, or just run `run-tests.sh`.

## Prerequisites per sample

- **languages/typescript**: Node 22+. The SDK is consumed via a `file:`
  dependency on `riot4k-api/build/dist/js/productionLibrary` — build it before
  installing.
- **languages/swift**: macOS with Xcode. Build the framework first:
  `./gradlew :riot4k-api:assembleRiot4KSDKReleaseXCFramework`.
- **platforms/linux**: build with
  `./gradlew -p samples :linux:linkReleaseExecutableLinuxX64`, then run
  `samples/platforms/linux/build/bin/linuxX64/releaseExecutable/linux.kexe`.
- **platforms/android**: an Android SDK (`local.properties` or `ANDROID_HOME`);
  the API key is injected as a `BuildConfig` field at build time.
