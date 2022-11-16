package com.example.traders.network.models.cryptoStatsData

data class Marketcap(
    val current_marketcap_usd: Double,
    val marketcap_dominance_percent: Double,
    val rank: Int
)
