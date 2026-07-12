# Riot4K

[![Build](https://github.com/lowbudgetlcs/Riot4K/actions/workflows/build.yml/badge.svg)](https://github.com/lowbudgetlcs/Riot4K/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)

**One Riot Games API client for every platform you ship.**

Riot4K is a Kotlin Multiplatform SDK built for *consistency*, *throughput*, and
*correctness*: the same client, the same rate limiter, and the same typed results
on the JVM, Android, iOS, macOS, Linux, and JavaScript — with idiomatic surfaces
for Kotlin, Java, TypeScript, and Swift.

## Why Riot4K

- **Write once, run everywhere.** Your match-history logic shouldn't behave one
  way in the backend and another way in the app. One codebase serves your
  server, your website, and your mobile apps — no more stitching together
  per-language API clients of varying quality.
- **Rate limiting that just works.** Riot4K learns your app and per-method
  limits live from Riot's response headers and enforces both tiers with
  sliding-window buckets. Choose the *burst* preset for latency or *throughput*
  for sustained load; `Retry-After` is always honored. Your key stays healthy
  under fire.
- **Errors are values, in every language.** A missing player is `NotFound`, not
  an exception. Results arrive as a sealed type in Kotlin, a pattern-matchable
  value in Java, a discriminated union in TypeScript, and an exhaustively
  switchable enum in Swift — the compiler makes sure you handled every case.
- **Built for production.** Only 429s, 5xx and transport errors are retried;
  everything else fails fast and typed. The public API is compiler-enforced
  explicit, every release is binary-compatibility-checked, and the whole
  codebase passes a zero-warning lint gate.
- **Proven in every language, on every change.** A cross-language contract
  suite runs the same scenarios through the real HTTP engines — OkHttp, Darwin,
  Node — from Kotlin, Java, TypeScript, and Swift test suites on every pull
  request. If a binding would break, CI knows before you do.
- **Coroutine-first.** Suspend functions end to end, safe to hammer from
  thousands of concurrent coroutines — they surface as `CompletableFuture` in
  Java, `Promise` in JS, and `async` in Swift.

## Who it's for

- Teams running a **stack**, not a script: a JVM backend, a web frontend, and
  mobile apps that should all see identical Riot data semantics.
- **High-traffic tools** — stats sites, tournament platforms, coaching apps —
  that need to squeeze a rate limit without tripping it.
- Anyone who wants Riot API plumbing to be **someone else's problem**, so they
  can focus on what to do with the data.

## Usage

### Kotlin

```kotlin
val riot4k = Riot4K.create(apiKey)

when (val account = riot4k.accountV1().getByRiotId(RegionalRoute.AMERICAS, "gameName", "tagLine")) {
    is RiotResult.Success -> println("puuid: ${account.data.puuid}")
    is RiotResult.NotFound -> println("No such riot ID")
    is RiotResult.Failure -> println("Request failed with status ${account.statusCode}")
}
```

### Java

Every endpoint has a `CompletableFuture` variant on `Riot4KAsync`; results stay
typed values you can pattern-match. See [`samples/java-sample`](samples/java-sample).

```java
try (var riot4k = Riot4KAsync.create(apiKey)) {
    var result = riot4k.accountV1().getByRiotIdAsync(RegionalRoute.AMERICAS, "gameName", "tagLine").join();
    if (result instanceof RiotResult.Success<AccountDto> success) {
        System.out.println(success.getData().getPuuid());
    }
}
```

### TypeScript / JavaScript

The JS distribution ships Promise-based classes with generated TypeScript
definitions; results are tagged objects. See [`samples/ts-sample`](samples/ts-sample).

```ts
const riot4k = new Riot4KJs(apiKey);
const result = await riot4k.accountV1().getByRiotId("AMERICAS", "gameName", "tagLine");
if (result.type === "success") console.log(result.account?.puuid);
riot4k.close();
```

### Swift

Build the XCFramework with `./gradlew :riot4k-api:assembleRiot4KReleaseXCFramework`
and add it to your project (SPM `binaryTarget` or direct embed); suspend
functions import as `async`, and results switch exhaustively. See
[`samples/swift-sample`](samples/swift-sample).

```swift
let result = try await riot4k.accountV1().getByRiotId(route: .americas, gameName: "gameName", tagLine: "tagLine")
switch onEnum(of: result) {
case .success(let success): print(success.data)
case .notFound: print("No such riot ID")
case .failure(let failure): print(failure.message)
}
```

Complete, tested example projects for all eight platforms live in
[`samples/`](samples/README.md).

## Installation

Riot4K is in **early development** and not yet published to Maven Central —
the first release lands there as `com.lowbudgetlcs:riot4k-api`. Until then,
consume it from source (`includeBuild`, as the [samples](samples/README.md) do)
or watch the repo for the `v0.1.0` release.

## Compatibility

Endpoint coverage is expanding; account-v1 is the reference implementation and
the remaining endpoints follow via schema-driven generation.

| API Endpoint          | Game                  | JVM | Android | iOS | macOS | Linux | JS |
| --------------------- | --------------------- | --- | ------- | --- | ----- | ----- | -- |
| `account-v1`          | Riftbound RSO         | ✅  | ✅      | ✅  | ✅    | ✅    | ✅  |
| `champion-mastery-v4` | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `champion-v3`         | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `clash-v1`            | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `league-exp-v4`       | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `league-v4`           | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lol-challenges-v1`   | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lol-rso-match-v1`    | League of Legends RSO | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lol-status-v4`       | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lor-deck-v1`         | Legends of Runeterra RSO | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lor-inventory-v1`    | Legends of Runeterra RSO | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lor-match-v1`        | Legends of Runeterra  | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lor-ranked-v1`       | Legends of Runeterra  | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `lor-status-v1`       | Legends of Runeterra  | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `match-v5`            | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `riftbound-content-v1`| Riftbound             | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `spectator-tft-v5`    | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `spectator-v5`        | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `summoner-v4`         | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `tft-league-v1`       | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `tft-match-v1`        | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `tft-status-v1`       | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `tft-summoner-v1`     | Teamfight Tactics RSO | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `tournament-stub-v5`  | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `tournament-v5`       | League of Legends Tournaments | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-console-match-v1`| VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-console-ranked-v1`| VALORANT             | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-content-v1`      | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-match-v1`        | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-ranked-v1`       | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-status-v1`       | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |

## Disclaimer

Riot4K is in early development — expect the API to evolve between releases.

Riot4K isn't endorsed by Riot Games and doesn't reflect the views or opinions of
Riot Games or anyone officially involved in producing or managing Riot Games
properties. Riot Games, and all associated properties are trademarks or
registered trademarks of Riot Games, Inc.
