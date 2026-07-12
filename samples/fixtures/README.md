# Fixture contract

These fixtures, served by `samples/mock-riot-server`, define the behavior every
language sample's test suite must verify. All suites run against the same server
and the same files, so the SDK's bindings provably behave identically in every
language.

## Endpoint

```
GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}
```

The `gameName` path segment selects the scenario:

| gameName        | Response |
| --------------- | -------- |
| `NotFound`      | `404` with `account-v1.by-riot-id.not-found.json` |
| `ServerError`   | `500` with `error.500.json` (every call) |
| `RateLimited`   | first call per tagLine: `429` with `Retry-After: 1` and `X-Rate-Limit-Type: application`; subsequent calls: `200` success |
| anything else   | `200` with `account-v1.by-riot-id.success.json`, `{gameName}`/`{tagLine}` placeholders substituted with the request's decoded path segments |

Successful responses carry Riot-style rate-limit headers
(`X-App-Rate-Limit: 20:1,100:120` and `X-Method-Rate-Limit: 2000:10`).

## Minimum scenario coverage per language suite

1. success — decoded body, `gameName`/`tagLine` echo back
2. `NotFound` — surfaces as the binding's not-found value, not an error
3. `ServerError` — surfaces as the binding's typed failure with status 500

## Server usage

- In-process (JVM suites): depend on the `:mock-riot-server` project and use
  `MockRiotServer(fixturesDir).start(port = 0)`, which returns the bound port.
- Out of process (everything else): run `samples/scripts/mock-server.sh start`,
  which launches the CLI, waits for readiness, and prints/exports the port; the
  CLI itself prints `MOCK_RIOT_SERVER_PORT=<port>` on stdout once bound.

Point the SDK at the server with `baseUrlTemplate("http://127.0.0.1:<port>")` —
the `{route}` placeholder is simply absent, so all routes hit the same server.
