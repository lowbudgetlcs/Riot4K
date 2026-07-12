package com.lowbudgetlcs.riot4k.samples.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lowbudgetlcs.riot4k.core.routes.RegionalRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AccountUiState {
    data object Idle : AccountUiState
    data class Loading(val riotId: String) : AccountUiState
    data class Result(val lookup: AccountLookup) : AccountUiState
}

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {
    private val _state = MutableStateFlow<AccountUiState>(AccountUiState.Idle)
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    fun load(gameName: String, tagLine: String) {
        _state.value = AccountUiState.Loading("$gameName#$tagLine")
        viewModelScope.launch {
            val lookup = repository.lookup(RegionalRoute.AMERICAS, gameName, tagLine)
            _state.value = AccountUiState.Result(lookup)
        }
    }

    companion object {
        fun factory(createRepository: () -> AccountRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AccountViewModel(createRepository()) as T
            }
    }
}
