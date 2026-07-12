package com.lowbudgetlcs.riot4k.mockserver

import java.nio.file.Path
import kotlin.io.path.exists

/**
 * CLI wrapper. Prints `MOCK_RIOT_SERVER_PORT=<port>` to stdout once bound (the
 * readiness signal consumers wait for), then blocks until terminated.
 *
 * Options: `--port=<n>` (default 0 = ephemeral; `PORT` env also honored) and
 * `--fixtures=<dir>` (default: `FIXTURES_DIR` env, then `../fixtures`, then
 * `samples/fixtures` relative to the working directory).
 */
fun main(args: Array<String>) {
    val port = args.firstNotNullOfOrNull { it.substringAfter("--port=", "").toIntOrNull() }
        ?: System.getenv("PORT")?.toIntOrNull()
        ?: 0
    val fixturesDir = args.firstNotNullOfOrNull {
        it.substringAfter("--fixtures=", "").ifEmpty { null }
    }
        ?.let(Path::of)
        ?: System.getenv("FIXTURES_DIR")?.let(Path::of)
        ?: listOf("../fixtures", "samples/fixtures", "fixtures").map(Path::of).firstOrNull { it.exists() }
        ?: error("Cannot locate the fixtures directory; pass --fixtures=<dir> or set FIXTURES_DIR")

    check(fixturesDir.exists()) { "Fixtures directory does not exist: $fixturesDir" }

    val server = MockRiotServer(fixturesDir)
    val boundPort = server.start(port)
    println("MOCK_RIOT_SERVER_PORT=$boundPort")
    Runtime.getRuntime().addShutdownHook(Thread { server.close() })
    Thread.currentThread().join()
}
