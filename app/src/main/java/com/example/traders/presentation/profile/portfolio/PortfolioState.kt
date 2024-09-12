package com.example.traders.presentation.profile.portfolio

import com.example.traders.database.Crypto
import com.example.traders.network.models.CryptoTicker
import com.github.mikephil.charting.data.PieEntry
import java.math.BigDecimal

data class PortfolioState(
    val chartData: List<PieEntry> = emptyList(),
    val cryptoListInUsd: List<CryptoInUsd> = emptyList(),
    val usdPricesFromBinance: List<CryptoTicker?> = emptyList(),
    val chartReadyForUpdate: Boolean = false,
    val chartDataLoaded: Boolean = false,
    val totalPortfolioBalance: BigDecimal? = null,
    val prevList: List<Crypto> = emptyList()
)