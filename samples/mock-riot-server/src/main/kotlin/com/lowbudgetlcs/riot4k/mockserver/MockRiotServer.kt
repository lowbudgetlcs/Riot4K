package com.lowbudgetlcs.riot4k.mockserver

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.readText

/**
 * Local stand-in for the Riot API, serving the fixtures in `samples/fixtures/`
 * according to the scenario table in that directory's README. Start on port 0
 * for an ephemeral port; [port] reports the actual binding.
 */
class MockRiotServer(private val fixturesDir: Path) : AutoCloseable {
    private var engine: EmbeddedServer<*, *>? = null
    private val rateLimitedCalls = ConcurrentHashMap<String, Int>()

    var port: Int = -1
        private set

    fun start(port: Int = 0): Int {
        val server = embeddedServer(CIO, port = port) {
            routing {
                get("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}") {
                    val gameName = call.parameters["gameName"].orEmpty()
                    val tagLine = call.parameters["tagLine"].orEmpty()
                    when (gameName) {
                        "NotFound" -> call.respondText(
                            fixture("account-v1.by-riot-id.not-found.json"),
                            ContentType.Application.Json,
                            HttpStatusCode.NotFound,
                        )
                        "ServerError" -> call.respondText(
                            fixture("error.500.json"),
                            ContentType.Application.Json,
                            HttpStatusCode.InternalServerError,
                        )
                        "RateLimited" -> {
                            val calls = rateLimitedCalls.merge(tagLine, 1, Int::plus) ?: 1
                            if (calls == 1) {
                                call.response.header("Retry-After", "1")
                                call.response.header("X-Rate-Limit-Type", "application")
                                call.respondText("", ContentType.Application.Json, HttpStatusCode.TooManyRequests)
                            } else {
                                call.respondSuccess(gameName, tagLine)
                            }
                        }
                        else -> call.respondSuccess(gameName, tagLine)
                    }
                }
            }
        }.start(wait = false)
        engine = server
        this.port = runBlocking { server.engine.resolvedConnectors().first().port }
        return this.port
    }

    private suspend fun io.ktor.server.application.ApplicationCall.respondSuccess(
        gameName: String,
        tagLine: String,
    ) {
        response.header("X-App-Rate-Limit", "20:1,100:120")
        response.header("X-Method-Rate-Limit", "2000:10")
        respondText(
            fixture("account-v1.by-riot-id.success.json")
                .replace("{gameName}", gameName)
                .replace("{tagLine}", tagLine),
            ContentType.Application.Json,
            HttpStatusCode.OK,
        )
    }

    private fun fixture(name: String): String = fixturesDir.resolve(name).readText()

    override fun close() {
        engine?.stop(gracePeriodMillis = 0, timeoutMillis = 1000)
    }
}
