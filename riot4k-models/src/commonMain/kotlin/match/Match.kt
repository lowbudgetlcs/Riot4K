package com.lowbudgetlcs.riot4k.models.match

import kotlinx.serialization.Serializable

@Serializable
data class Match(
    val matchId: String,
    val gameVersion: String
)