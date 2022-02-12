package com.example.traders.network

import com.example.traders.profile.cryptoData.CryptoTicker
import com.example.traders.watchlist.cryptoData.ServerTime
import com.example.traders.watchlist.cryptoData.binance24HourData.Binance24Data
import com.example.traders.watchlist.cryptoData.binance24HourData.Binance24DataItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {

    @GET("api/v3/time")
    suspend fun checkServerTime(): Response<ServerTime>

    @GET("/api/v3/ticker/24hr")
    suspend fun get24HourData(): Response<List<Binance24DataItem>>

    @GET("/api/v3/ticker/price")
    suspend fun getBinanceTickerBySymbol(
        @Query("symbol") symbol: String
    ): Response<CryptoTicker>
}