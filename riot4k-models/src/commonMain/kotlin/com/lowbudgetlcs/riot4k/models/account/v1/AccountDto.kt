package com.lowbudgetlcs.riot4k.models.account.v1

import kotlinx.serialization.Serializable

/**
 * A Riot account, as returned by account-v1 endpoints.
 *
 * [gameName] and [tagLine] together form the riot ID (`gameName#tagLine`);
 * either can be absent when the account has no active riot ID.
 */
@Serializable
public data class AccountDto(
    val puuid: String,
    val gameName: String? = null,
    val tagLine: String? = null,
)
