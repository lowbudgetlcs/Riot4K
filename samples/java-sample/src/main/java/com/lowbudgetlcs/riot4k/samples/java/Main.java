package com.lowbudgetlcs.riot4k.samples.java;

import com.lowbudgetlcs.riot4k.api.java.Riot4KAsync;
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute;

/**
 * Looks up a riot ID and prints the account.
 *
 * <p>Usage: {@code RIOT_API_KEY=RGAPI-... ./gradlew -p samples :java-sample:run}
 */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        String apiKey = System.getenv("RIOT_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set the RIOT_API_KEY environment variable to your Riot API key");
        }
        String gameName = args.length > 0 ? args[0] : "Hide on bush";
        String tagLine = args.length > 1 ? args[1] : "KR1";

        try (Riot4KAsync riot4k = Riot4KAsync.create(apiKey)) {
            AccountRepository repository = new AccountRepository(riot4k);
            AccountLookup lookup = repository.lookup(RegionalRoute.AMERICAS, gameName, tagLine).join();
            switch (lookup) {
                case AccountLookup.Found found ->
                    System.out.println(found.riotId() + " -> puuid=" + found.puuid());
                case AccountLookup.Missing missing ->
                    System.out.println("No account with riot ID " + gameName + "#" + tagLine);
                case AccountLookup.Error error ->
                    System.out.println("Lookup failed: " + error.message());
            }
        }
    }
}
