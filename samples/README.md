# Riot4K Samples

Each sample is a complete, standalone consumer of the SDK with its own layered
architecture (an `AccountRepository` mapping SDK results onto app-level states)
and its own test suite. The Kotlin samples consume the SDK from source via an
included build; the TypeScript, Java, and Swift samples consume the SDK's real
distribution artifacts — exactly like an external user.

Run everything from the repository root. Live runs read the API key from the
`RIOT_API_KEY` environment variable — no key is ever committed.

| Sample | Language / platform | Run live | Run tests |
| ------ | ------------------- | -------- | --------- |
| `jvm-sample` | Kotlin / JVM CLI | `RIOT_API_KEY=... ./gradlew -p samples :jvm-sample:run --args="gameName tagLine"` | `./gradlew -p samples :jvm-sample:test` |
| `java-sample` | Java 21 CLI | `RIOT_API_KEY=... ./gradlew -p samples :java-sample:run` | `./gradlew -p samples :java-sample:test` |
| `android-sample` | Kotlin / Android app | `RIOT_API_KEY=... ./gradlew -p samples :android-sample:installDebug` | `./gradlew -p samples :android-sample:testDebugUnitTest` |
| `js-sample` | Kotlin / Node.js | `RIOT_API_KEY=... ./gradlew -p samples :js-sample:jsNodeRun` | `./gradlew -p samples :js-sample:jsTest` |
| `ts-sample` | TypeScript / Node.js | `RIOT_API_KEY=... npm run live` (in `samples/ts-sample`, after `npm install`) | `npm test` (see below) |
| `linux-sample` | Kotlin/Native / Linux | build then run the `.kexe` (see below) | `./gradlew -p samples :linux-sample:linuxX64Test` |
| `ios-sample` | Kotlin / iOS framework | consumed from Xcode | compile-checked in CI |
| `swift-sample` | Swift (SPM + XCTest) | — | `samples/swift-sample/run-tests.sh` (macOS) |

## Contract tests and the mock Riot server

Test suites run against a local stand-in for the Riot API so every language
binding provably maps the same responses to the same states. The behaviors and
fixtures are defined in [`fixtures/README.md`](fixtures/README.md).

- JVM-hosted suites (`jvm-sample`, `java-sample`, `android-sample`) embed the
  server in-process; nothing to start manually.
- `ts-sample`'s vitest setup spawns it automatically (build it once first with
  `./gradlew -p samples :mock-riot-server:installDist`, and build the JS
  distribution with `./gradlew :riot4k-api:jsNodeProductionLibraryDistribution`
  before `npm install`).
- `swift-sample` expects the server's port in `MOCK_RIOT_PORT`; use
  `scripts/mock-server.sh start` / `stop`, or just run `run-tests.sh`.

## Prerequisites per sample

- **ts-sample**: Node 22+. The SDK is consumed via a `file:` dependency on
  `riot4k-api/build/dist/js/productionLibrary` — build it before installing.
- **swift-sample**: macOS with Xcode. Build the framework first:
  `./gradlew :riot4k-api:assembleRiot4KReleaseXCFramework`.
- **linux-sample**: build with
  `./gradlew -p samples :linux-sample:linkReleaseExecutableLinuxX64`, then run
  `samples/linux-sample/build/bin/linuxX64/releaseExecutable/linux-sample.kexe`.
- **android-sample**: an Android SDK (`local.properties` or `ANDROID_HOME`);
  the API key is injected as a `BuildConfig` field at build time.
