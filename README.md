# Riot4K

## Disclaimer

This project is in early development stages, **use at your own risk**.

## Our Goals

We aim for Riot4k to be a modern, type-safe, and asynchronous Riot Games API SDK, built entirely in Kotlin.

We wish to be the *first and only* Riot library capable of sharing logic. Truly write once, run everywhere. No more writing separate API clients for the app and the server - or even worse, picking up different libraries of varying qualities.

We want to enable high-traffic applications by properly applying Coroutines.

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

## Compatibility

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
| `val-console-ranked-v1`| VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-content-v1`      | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-match-v1`        | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-ranked-v1`       | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
| `val-status-v1`       | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌    | ❌  |
