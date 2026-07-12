import { Riot4KJs } from "riot4k-api";

/** Application-level outcome of an account lookup. */
export type AccountLookup =
  | { kind: "found"; puuid: string; riotId: string }
  | { kind: "missing" }
  | { kind: "error"; message: string };

/**
 * The application's account access layer: maps SDK results onto the app's own
 * states so the rest of the app never handles raw API types.
 */
export class AccountRepository {
  constructor(private readonly riot4k: Riot4KJs) {}

  async lookup(route: string, gameName: string, tagLine: string): Promise<AccountLookup> {
    const result = await this.riot4k.accountV1().getByRiotId(route, gameName, tagLine);
    switch (result.type) {
      case "success": {
        const account = result.account;
        if (account == null) {
          return { kind: "error", message: "success result without an account" };
        }
        return {
          kind: "found",
          puuid: account.puuid,
          riotId: `${account.gameName ?? gameName}#${account.tagLine ?? tagLine}`,
        };
      }
      case "notFound":
        return { kind: "missing" };
      default:
        return {
          kind: "error",
          message: result.message ?? `request failed with status ${result.statusCode}`,
        };
    }
  }
}
