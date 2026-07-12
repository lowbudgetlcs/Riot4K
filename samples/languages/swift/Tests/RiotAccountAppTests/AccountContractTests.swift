import XCTest
@testable import RiotAccountApp

/// Contract tests: the Swift binding exercised end-to-end through the real
/// XCFramework and Darwin HTTP engine against the shared mock Riot server.
///
/// Requires the server to be running with its port in `MOCK_RIOT_PORT`
/// (see `samples/scripts/mock-server.sh start`); skips otherwise.
final class AccountContractTests: XCTestCase {
    private var repository: AccountRepository!

    override func setUpWithError() throws {
        guard
            let portString = ProcessInfo.processInfo.environment["MOCK_RIOT_PORT"],
            let port = Int(portString)
        else {
            throw XCTSkip("MOCK_RIOT_PORT not set; run samples/scripts/mock-server.sh start first")
        }
        repository = AccountRepository(
            apiKey: "test-key",
            baseUrlTemplate: "http://127.0.0.1:\(port)",
            maxRetries: 0
        )
    }

    override func tearDown() {
        repository?.close()
        repository = nil
    }

    func testSuccessfulLookupMapsToFoundWithEchoedRiotId() async throws {
        let lookup = try await repository.lookup(gameName: "Hide on bush", tagLine: "KR1")
        guard case let .found(puuid, riotId) = lookup else {
            return XCTFail("expected found, got \(lookup)")
        }
        XCTAssertEqual(riotId, "Hide on bush#KR1")
        XCTAssertTrue(puuid.hasPrefix("mock-puuid-"))
    }

    func testUnknownRiotIdMapsToMissingNotError() async throws {
        let lookup = try await repository.lookup(gameName: "NotFound", tagLine: "x")
        XCTAssertEqual(lookup, .missing)
    }

    func testApiFailureMapsToErrorWithStatus() async throws {
        let lookup = try await repository.lookup(gameName: "ServerError", tagLine: "x")
        guard case let .error(message) = lookup else {
            return XCTFail("expected error, got \(lookup)")
        }
        XCTAssertTrue(message.contains("500"))
    }
}
