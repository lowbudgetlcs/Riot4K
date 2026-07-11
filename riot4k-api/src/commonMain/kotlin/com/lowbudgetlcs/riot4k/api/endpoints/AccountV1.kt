package com.lowbudgetlcs.riot4k.api.endpoints

import com.lowbudgetlcs.riot4k.core.RiotHttpClient
import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import com.lowbudgetlcs.riot4k.models.account.v1.AccountDto

/**
 * account-v1 endpoints. Obtain via [com.lowbudgetlcs.riot4k.api.Riot4K.accountV1].
 */
public class AccountV1 internal constructor(
    private val client: RiotHttpClient,
) {
    /**
     * Gets an account by its riot ID (`gameName#tagLine`).
     *
     * `GET /riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}`
     *
     * Returns [RiotResult.NotFound] when no account has that riot ID.
     */
    public suspend fun getByRiotId(
        route: RegionalRoute,
        gameName: String,
        tagLine: String,
    ): RiotResult<AccountDto> = client.get(
        route,
        METHOD_GET_BY_RIOT_ID,
        "riot", "account", "v1", "accounts", "by-riot-id", gameName, tagLine,
    )

    private companion object {
        const val METHOD_GET_BY_RIOT_ID = "account-v1.getByRiotId"
    }
}
