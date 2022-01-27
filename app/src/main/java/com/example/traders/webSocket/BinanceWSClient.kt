package com.example.traders.webSocket

import com.example.traders.watchlist.cryptoData.binance24hTickerData.PriceTicker
import com.example.traders.watchlist.cryptoData.binance24hTickerData.PriceTickerData
import kotlinx.coroutines.flow.SharedFlow

interface BinanceWSClient {
    val state: SharedFlow<PriceTickerData>
    fun subscribe(params: List<String>, type: String)
    fun unsubscribe(params: List<String>, type: String)
    fun startConnection()
    fun stopConnection()
    fun restartConnection()
}

