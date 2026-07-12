@file:OptIn(ExperimentalJsExport::class)

package com.lowbudgetlcs.riot4k.api.js

import com.lowbudgetlcs.riot4k.core.result.RiotResult
import com.lowbudgetlcs.riot4k.models.account.v1.AccountDto

/**
 * Outcome of an account lookup, flattened for JavaScript consumers as a tagged
 * object: switch on [type] (`"success"` | `"notFound"` | `"failure"`).
 *
 * [account] is non-null exactly when [type] is `"success"`; the failure fields
 * ([statusCode], [rateLimitType], [message]) are populated only for `"failure"`.
 */
@JsExport
public class AccountResultJs internal constructor(
    public val type: String,
    public val account: AccountJs?,
    public val statusCode: Int?,
    public val retries: Int,
    public val rateLimitType: String?,
    public val message: String?,
)

/** A Riot account. [gameName]#[tagLine] form the riot ID; either can be absent. */
@JsExport
public class AccountJs internal constructor(
    public val puuid: String,
    public val gameName: String?,
    public val tagLine: String?,
)

internal fun RiotResult<AccountDto>.toJs(): AccountResultJs = when (this) {
    is RiotResult.Success -> AccountResultJs(
        type = "success",
        account = AccountJs(data.puuid, data.gameName, data.tagLine),
        statusCode = null,
        retries = 0,
        rateLimitType = null,
        message = null,
    )
    is RiotResult.NotFound -> AccountResultJs(
        type = "notFound",
        account = null,
        statusCode = null,
        retries = 0,
        rateLimitType = null,
        message = null,
    )
    is RiotResult.Failure -> AccountResultJs(
        type = "failure",
        account = null,
        statusCode = statusCode,
        retries = retries,
        rateLimitType = rateLimitType,
        message = message,
    )
}
