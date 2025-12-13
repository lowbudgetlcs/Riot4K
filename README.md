# riot4K

## Disclaimer

This project is in early development stages, **use at your own risk**.

## Our Goals

We aim for riot4K to be a modern, type-safe, and asynchronous Riot Games API SDK, built entirely in Kotlin.

We wish to be the *first and only* Riot library capable of sharing logic. Truly write once, run everywhere. No more writing separate API clients for the app and the server - or even worse, picking up different libraries of varying qualities.

We want to enable high-traffic applications by properly applying Coroutines.

## Compatibility

| API Endpoint          | Game                  | JVM | Android | iOS | Linux | JS |
| --------------------- | --------------------- | --- | ------- | --- | ----- | -- |
| `account-v1`          | Riftbound RSO         | ❌  | ❌      | ❌  | ❌    | ❌  |
| `champion-mastery-v4` | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `champion-v3`         | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `clash-v1`            | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `league-exp-v4`       | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `league-v4`           | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lol-challenges-v1`   | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lol-rso-match-v1`    | League of Legends RSO | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lol-status-v4`       | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lor-deck-v1`         | Legends of Runeterra RSO | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lor-inventory-v1`    | Legends of Runeterra RSO | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lor-match-v1`        | Legends of Runeterra  | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lor-ranked-v1`       | Legends of Runeterra  | ❌  | ❌      | ❌  | ❌    | ❌  |
| `lor-status-v1`       | Legends of Runeterra  | ❌  | ❌      | ❌  | ❌    | ❌  |
| `match-v5`            | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `riftbound-content-v1`| Riftbound             | ❌  | ❌      | ❌  | ❌    | ❌  |
| `spectator-tft-v5`    | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `spectator-v5`        | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `summoner-v4`         | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `tft-league-v1`       | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `tft-match-v1`        | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `tft-status-v1`       | Teamfight Tactics     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `tft-summoner-v1`     | Teamfight Tactics RSO | ❌  | ❌      | ❌  | ❌    | ❌  |
| `tournament-stub-v5`  | League of Legends     | ❌  | ❌      | ❌  | ❌    | ❌  |
| `tournament-v5`       | League of Legends Tournaments | ❌  | ❌      | ❌  | ❌    | ❌  |
| `val-console-match-v1`| VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌  |
| `val-console-ranked-v1`| VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌  |
| `val-content-v1`      | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌  |
| `val-match-v1`        | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌  |
| `val-ranked-v1`       | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌  |
| `val-status-v1`       | VALORANT              | ❌  | ❌      | ❌  | ❌    | ❌  |
