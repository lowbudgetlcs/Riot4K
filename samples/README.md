# Riot4K Samples

Each sample is a standalone consumer of the SDK, wired to the local modules via
an included build. All samples read the API key from the `RIOT_API_KEY`
environment variable — no key is ever committed.

Run everything from the repository root.

## JVM

```sh
RIOT_API_KEY=RGAPI-... ./gradlew -p samples :jvm-sample:run --args="gameName tagLine"
```

## Linux (Kotlin/Native)

```sh
./gradlew -p samples :linux-sample:linkReleaseExecutableLinuxX64
RIOT_API_KEY=RGAPI-... samples/linux-sample/build/bin/linuxX64/releaseExecutable/linux-sample.kexe gameName tagLine
```

## JS (Node.js)

```sh
RIOT_API_KEY=RGAPI-... ./gradlew -p samples :js-sample:jsNodeRun
```

## Android

The key is injected as a `BuildConfig` field at build time:

```sh
RIOT_API_KEY=RGAPI-... ./gradlew -p samples :android-sample:installDebug
```

## iOS

`ios-sample` produces a `Riot4KSample.framework` for use from Xcode:

```sh
./gradlew -p samples :ios-sample:linkDebugFrameworkIosSimulatorArm64
```

Add the framework (from `samples/ios-sample/build/bin/iosSimulatorArm64/debugFramework`)
to an Xcode project, then call it from Swift — suspend functions import as async:

```swift
import Riot4KSample

let sample = AccountSample(apiKey: ProcessInfo.processInfo.environment["RIOT_API_KEY"] ?? "")
let description = try await sample.describeAccount(gameName: "Hide on bush", tagLine: "KR1")
print(description)
sample.close()
```
