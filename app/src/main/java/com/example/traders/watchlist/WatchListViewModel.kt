package com.example.traders.watchlist

import android.util.Log
import androidx.lifecycle.asLiveData
import com.example.traders.BaseViewModel
import com.example.traders.database.FavouriteCrypto
import com.example.traders.database.FilterPreferences
import com.example.traders.database.PreferancesManager
import com.example.traders.database.SortOrder
import com.example.traders.repository.CryptoRepository
import com.example.traders.repository.enumContains
import com.example.traders.watchlist.cryptoData.FixedCryptoList
import com.example.traders.watchlist.cryptoData.binance24HourData.Binance24DataItem
import com.example.traders.watchlist.cryptoData.binance24HourData.BinanceDataItem
import com.example.traders.watchlist.cryptoData.binance24hTickerData.PriceTickerData
import com.example.traders.webSocket.BinanceWSClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val webSocketClient: BinanceWSClient,
    private val watchListRepository: WatchListRepository,
    private val preferencesManager: PreferancesManager
) : BaseViewModel() {

    private val _state = watchListRepository.binanceCryptoList.
    val state = _state.asStateFlow()

    init {
        launchWithProgress {
            watchListRepository.refreshCryptoPrices()
        }
//        startCollectingBinanceTickerData()
    }


    fun getCryptoOnRefresh() {
        launch {
            _state.update { it.copy(isRefreshing = true) }
            watchListRepository.refreshCryptoPrices()
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun onFavouriteButtonClicked() {
        launch {
            preferencesManager.updateIsFavourite(!_state.value.showFavourites)
        }
    }

    fun getLatestPrefs() {
        launch {
            val latestVals = preferencesManager.preferencesFlow.last()
            _state.update { it.copy(isRefreshing = latestVals.isFavourite) }
        }
    }

    private suspend fun startCollectingPreferences() {
        preferencesFlow.collect { prefs ->
            sortList(prefs)
        }
    }

    private fun sortList(preferences: FilterPreferences) {
        when (preferences.sortOrder) {
            SortOrder.DEFAULT -> emitDefaultList()
            SortOrder.BY_NAME_ASC -> emitSortedByNameAsc()
            SortOrder.BY_NAME_DESC -> emitSortedByNameDesc()
            SortOrder.BY_CHANGE_ASC -> emitSortedByChangeAsc()
            SortOrder.BY_CHANGE_DESC -> emitSortedByChangeDesc()
        }
    }

    private fun emitDefaultList() {
//        TODO: sort by enum order
//        val sortedList = _state.value.binanceCryptoData.sortedBy { it.isFavourite }
//        _state.value = _state.value.copy(binanceCryptoData = sortedList)
    }

    private fun emitSortedByNameAsc() {
        _state.value = _state.value.copy(binanceCryptoData = _state.value.binanceCryptoData.sortedBy { it.symbol })
    }

    private fun emitSortedByNameDesc() {
        _state.value = _state.value.copy(binanceCryptoData = _state.value.binanceCryptoData.sortedByDescending { it.symbol })
    }

    private fun emitSortedByChangeAsc() {
        _state.value = _state.value.copy(binanceCryptoData = _state.value.binanceCryptoData.sortedBy { it.priceChangePercent })
    }

    private fun emitSortedByChangeDesc() {
        _state.value = _state.value.copy(binanceCryptoData = _state.value.binanceCryptoData.sortedByDescending { it.priceChangePercent })
    }

    // This function cannot be called since connection hasnt been established yet at this point
    // Subscribe and unsubscribe must be called when connection is successfully established and terminated respectively
    private fun subscribeWebSocket() {
        Log.e("ALlCryptoViewModel", "initWebSocket called")
        webSocketClient.subscribe(listOf("btcusdt", "bnbusdt"), "ticker")
    }
}

private fun List<BinanceDataItem>.applyFavourites(favouriteList: List<FavouriteCrypto>?): List<BinanceDataItem> {
    return map { item ->
        item.copy(
            isFavourite = favouriteList?.any { it.symbol == item.symbol.replace("USDT", "") } == true
        )
    }
}

private fun Binance24DataItem.toBinanceDataItem(): BinanceDataItem {
    return BinanceDataItem(
        symbol = symbol,
        last = last,
        high = high,
        low = low,
        open = open,
        priceChange = priceChange,
        priceChangePercent = priceChangePercent
    )
}
