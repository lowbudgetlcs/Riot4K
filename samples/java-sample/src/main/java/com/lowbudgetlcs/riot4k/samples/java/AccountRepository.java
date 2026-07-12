package com.lowbudgetlcs.riot4k.samples.java;

import com.lowbudgetlcs.riot4k.api.java.Riot4KAsync;
import com.lowbudgetlcs.riot4k.core.result.RiotResult;
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute;
import com.lowbudgetlcs.riot4k.models.account.v1.AccountDto;

import java.util.concurrent.CompletableFuture;

/**
 * The application's account access layer: maps SDK results onto the app's own
 * {@link AccountLookup} states so the rest of the app never handles raw API types.
 */
public final class AccountRepository {
    private final Riot4KAsync riot4k;

    public AccountRepository(Riot4KAsync riot4k) {
        this.riot4k = riot4k;
    }

    public CompletableFuture<AccountLookup> lookup(RegionalRoute route, String gameName, String tagLine) {
        return riot4k.accountV1()
            .getByRiotIdAsync(route, gameName, tagLine)
            .thenApply(result -> map(result, gameName, tagLine));
    }

    private static AccountLookup map(RiotResult<AccountDto> result, String gameName, String tagLine) {
        if (result instanceof RiotResult.Success<AccountDto> success) {
            AccountDto account = success.getData();
            String riotId = (account.getGameName() != null ? account.getGameName() : gameName)
                + "#" + (account.getTagLine() != null ? account.getTagLine() : tagLine);
            return new AccountLookup.Found(account.getPuuid(), riotId);
        }
        if (result instanceof RiotResult.NotFound) {
            return new AccountLookup.Missing();
        }
        RiotResult.Failure failure = (RiotResult.Failure) result;
        return new AccountLookup.Error(failure.getMessage());
    }
}
