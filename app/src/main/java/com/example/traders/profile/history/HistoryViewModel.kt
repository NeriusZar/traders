package com.example.traders.profile.history

import com.example.traders.BaseViewModel
import com.example.traders.network.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CryptoRepository
): BaseViewModel() {
    val transactionList = repository.getAllTransactionsLive()

    fun clearHistory() : Unit {
        launch {
            repository.deleteAllTransactions()
        }
    }
}