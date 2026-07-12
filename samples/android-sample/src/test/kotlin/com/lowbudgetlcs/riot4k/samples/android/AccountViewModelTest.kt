package com.lowbudgetlcs.riot4k.samples.android

import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRepository(private val lookup: AccountLookup) : AccountRepository {
        override suspend fun lookup(route: RegionalRoute, gameName: String, tagLine: String) = lookup
    }

    @Test
    fun loadTransitionsFromLoadingToResult() = runTest(dispatcher) {
        val found = AccountLookup.Found("puuid-1", "Hide on bush#KR1")
        val viewModel = AccountViewModel(FakeRepository(found))

        viewModel.load("Hide on bush", "KR1")
        val loading = assertIs<AccountUiState.Loading>(viewModel.state.value)
        assertEquals("Hide on bush#KR1", loading.riotId)

        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(AccountUiState.Result(found), viewModel.state.value)
    }

    @Test
    fun errorLookupSurfacesInState() = runTest(dispatcher) {
        val viewModel = AccountViewModel(FakeRepository(AccountLookup.Error("boom")))

        viewModel.load("x", "y")
        dispatcher.scheduler.advanceUntilIdle()

        val result = assertIs<AccountUiState.Result>(viewModel.state.value)
        assertEquals(AccountLookup.Error("boom"), result.lookup)
    }
}
