package com.example.traders.watchlist.allCrypto.singleCryptoScreen.priceStatisticsTab

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.traders.BaseViewModel
import com.example.traders.network.RetrofitInstance
import com.example.traders.watchlist.cryptoData.cryptoStatsData.CryptoStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CryptoPriceStatisticsViewModel @Inject constructor() : BaseViewModel() {
    private val _cryptoStatsResponse = MutableLiveData<CryptoStatistics>()
    val cryptoStatsResponse
        get() = _cryptoStatsResponse

    fun fetchCryptoPriceStatistics(slug: String) {
        viewModelScope.launch {

            var response = try {
                RetrofitInstance.api.getCryptoPriceStatistics(slug)
            } catch (e: IOException) {
                Log.d("Response", "IOException, internet connection interference: ${e}")
                return@launch
            } catch (e: HttpException) {
                Log.d("Response", "HttpException, unexpected response: ${e}")
                return@launch
            }

            if (response.isSuccessful && response.body() != null) {
                val responseData = response.body()
                _cryptoStatsResponse.value = responseData
            } else {
                Log.d("Response", "Response not successful")
            }

        }
    }
}