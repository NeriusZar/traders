package com.example.traders.dialogs.buyDialog

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.traders.dialogs.Constants
import com.example.traders.dialogs.DialogValidationMessage
import com.example.traders.repository.CryptoRepository
import com.example.traders.roundNum
import com.example.traders.watchlist.cryptoData.FixedCryptoList
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode

class BuyDialogViewModel @AssistedInject constructor(
    val repository: CryptoRepository,
    @Assisted val symbol: String,
    @Assisted val lastPrice: BigDecimal
) : ViewModel() {
    private val priceToRound = getPriceToRound()
    private val amountToRound = getAmountToRound()
    private val usdBalance = getBalance()

    private val _state = MutableStateFlow(BuyState(
        usdBalance = usdBalance,
        priceNumToRound = priceToRound,
        amountToRound = amountToRound
    ))
    val state = _state.asStateFlow()

    fun updateBalance() {
//         updates crypto balance
        repository.setStoredPrice(Constants.USD_BALANCE_KEY, _state.value.usdLeft.toFloat())
//         updates usd balance
        val newCryptoBalance = repository.getStoredTag(symbol) + _state.value.cryptoToGet.toFloat()
        repository.setStoredPrice(symbol, newCryptoBalance)
    }

    fun add1000UsdToBalance() {
        repository.setStoredPrice(Constants.USD_BALANCE_KEY, 1000F)
    }

    fun validateInput(enteredVal: String) {
        val decimalEnteredVal = BigDecimal(enteredVal)
        if (enteredVal.isBlank()) {
            _state.value = _state.value.copy(
                isBtnEnabled = false,
                messageType = DialogValidationMessage.IS_EMPTY
            )
        } else if (decimalEnteredVal > usdBalance) {
            _state.value = _state.value.copy(
                isBtnEnabled = false,
                messageType = DialogValidationMessage.IS_TOO_HIGH
            )
        } else if (decimalEnteredVal < _state.value.minInputVal) {
            _state.value = _state.value.copy(
                isBtnEnabled = false,
                messageType = DialogValidationMessage.IS_TOO_LOW
            )
        } else {
            _state.value = _state.value.copy(
                isBtnEnabled = true,
                messageType = DialogValidationMessage.IS_VALID
            )
        }

        // Input val is set to 0 if nothing entered
        if (enteredVal.isBlank()) {
            _state.value = _state.value.copy(inputVal = BigDecimal(0))
        } else {
            _state.value = _state.value.copy(inputVal = decimalEnteredVal)
        }
        calculateNewBalance()
    }

    private fun getPriceToRound() = FixedCryptoList.valueOf(symbol).priceToRound
    private fun getAmountToRound() = FixedCryptoList.valueOf(symbol).amountToRound

    private fun calculateNewBalance() {
        var usdLeft = usdBalance
        var cryptoToGet = BigDecimal(0.0)

        if (_state.value.messageType == DialogValidationMessage.IS_VALID) {
            usdLeft = usdBalance - _state.value.inputVal
            // TODO apply updated roundFunction() and provide numOfDigits(int) from fixedCryptoList by symbol to round
            cryptoToGet = _state.value.inputVal.divide(lastPrice, amountToRound, BigDecimal.ROUND_HALF_UP)
        }

        Log.e("TAG", "USD LEFT $cryptoToGet, ${_state.value.inputVal.toString()}, ${lastPrice.toString()}")
        Log.e("TAG", "USD LEFT ${cryptoToGet.toFloat()}")
//        Log.e("TAG", "USD LEFT ${cryptoToGet.roundNum(_state.value.amountToRound)}")
//        Log.e("TAG", "USD LEFT ${cryptoToGet.roundNum(_state.value.amountToRound).toFloat()}")
//        Log.e("TAG", "USD LEFT ${cryptoToGet.roundNum(_state.value.amountToRound).toFloat().toDouble()}")
//        Log.e("TAG", "USD LEFT ${cryptoToGet.roundNum(_state.value.amountToRound).toFloat().toString().toDouble()}")

        _state.value = _state.value.copy(
            usdLeft = usdLeft.roundNum(),
            cryptoToGet = cryptoToGet.roundNum(_state.value.amountToRound)
        )
    }

    private fun getBalance() = repository.getStoredTag(Constants.USD_BALANCE_KEY).toBigDecimal().roundNum()

    @AssistedFactory
    interface Factory {
        fun create(symbol: String, lastPrice: BigDecimal): BuyDialogViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            symbol: String,
            lastPrice: BigDecimal
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(symbol, lastPrice) as T
            }
        }
    }
}