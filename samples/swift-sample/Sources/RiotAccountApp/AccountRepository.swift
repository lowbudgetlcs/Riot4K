import Foundation
import Riot4K

/// Application-level outcome of an account lookup.
public enum AccountLookup: Equatable {
    case found(puuid: String, riotId: String)
    case missing
    case error(message: String)
}

/// The application's account access layer: maps SDK results onto the app's own
/// ``AccountLookup`` states so the rest of the app never handles raw API types.
public final class AccountRepository {
    private let riot4k: Riot4K

    public init(apiKey: String, baseUrlTemplate: String? = nil, maxRetries: Int32 = 3) {
        var builder = Riot4KConfig.Builder(apiKey: apiKey)
        if let baseUrlTemplate {
            builder = builder.baseUrlTemplate(value: baseUrlTemplate)
        }
        builder = builder.maxRetries(value: maxRetries)
        riot4k = Riot4K(config: builder.build())
    }

    public func lookup(gameName: String, tagLine: String) async throws -> AccountLookup {
        let result = try await riot4k.accountV1().getByRiotId(
            route: .americas,
            gameName: gameName,
            tagLine: tagLine
        )
        // Exhaustive switch over the sealed result via SKIE's onEnum bridging.
        // Without SKIE, replace with `is RiotResultSuccess` class checks.
        switch onEnum(of: result) {
        case .success(let success):
            guard let account = success.data as? AccountDto else {
                return .error(message: "success result without an account")
            }
            let riotId = "\(account.gameName ?? gameName)#\(account.tagLine ?? tagLine)"
            return .found(puuid: account.puuid, riotId: riotId)
        case .notFound:
            return .missing
        case .failure(let failure):
            return .error(message: failure.message)
        }
    }

    public func close() {
        riot4k.close()
    }
}
