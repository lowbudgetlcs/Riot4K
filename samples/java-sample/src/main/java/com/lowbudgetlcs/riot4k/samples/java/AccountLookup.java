package com.lowbudgetlcs.riot4k.samples.java;

/** Application-level outcome of an account lookup. */
public sealed interface AccountLookup {
    record Found(String puuid, String riotId) implements AccountLookup {}

    record Missing() implements AccountLookup {}

    record Error(String message) implements AccountLookup {}
}
