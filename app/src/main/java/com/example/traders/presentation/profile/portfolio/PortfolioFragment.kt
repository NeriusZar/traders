package com.example.traders.presentation.profile.portfolio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.traders.presentation.BaseFragment
import com.example.traders.R
import com.example.traders.database.Crypto
import com.example.traders.databinding.FragmentPortfolioBinding
import com.example.traders.presentation.dialogs.confirmationDialog.ConfirmationDialogFragment
import com.example.traders.presentation.dialogs.confirmationDialog.ConfirmationType
import com.example.traders.presentation.dialogs.depositDialog.DepositDialogFragment
import com.example.traders.presentation.profile.ProfileFragmentDirections
import com.example.traders.presentation.profile.adapters.PortfolioListAdapter
import com.example.traders.presentation.watchlist.adapters.SingleCryptoListener
import com.example.traders.utils.Colors
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class PortfolioFragment : BaseFragment() {
    private lateinit var binding: FragmentPortfolioBinding
    val viewModel: PortfolioViewModel by viewModels()
    private lateinit var adapter: PortfolioListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPortfolioBinding.inflate(inflater, container, false)
        binding.setUpPieChart()
        binding.setUpClickListeners()
        binding.setUpAdapter()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener(
            "deposit_data"
        ) { _, bundle ->
            val depositedAmount = bundle.getString("deposited_amount") ?: "0"
            showConfirmationDialog(depositedAmount)
        }
        updateChartAndAdapterData()
        // Update portfolio on list change
        viewModel.livePortfolioList.observe(viewLifecycleOwner) {
            it?.let {
                binding.updateMessageVisibility(it)
                viewModel.updatePortfolioState()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.state.collect {
                binding.updateUiData(it)
            }
        }
    }

    private fun showConfirmationDialog(depositedAmount: String) {
        val confirmationDialog = ConfirmationDialogFragment(
            message = DEPOSIT_CONFIRMATION_MESSAGE,
            confirmationType = ConfirmationType.DepositUsd(depositedAmount.toBigDecimal())
        )
        confirmationDialog.show(parentFragmentManager, "deposit_dialog")
    }

    private fun FragmentPortfolioBinding.setUpPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.setUsePercentValues(true)
        pieChart.setCenterTextSize(20F)
        pieChart.setDrawEntryLabels(false)
        pieChart.description.isEnabled = false
        pieChart.legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        pieChart.legend.orientation = Legend.LegendOrientation.VERTICAL
    }

    private fun FragmentPortfolioBinding.updateUiData(state: PortfolioState) {
        if (state.chartReadyForUpdate) {
            val pieDataSet = PieDataSet(state.chartData, "Portfolio")
            pieDataSet.setColors(Colors.pieChartColors)
            pieChart.data = PieData(pieDataSet)
            pieChart.invalidate()
            pieChart.animate()
            viewModel.chartUpdated()
            adapter.addHeaderAndSubmitList(state.cryptoListInUsd)
        }

        state.totalPortfolioBalance?.let {
            totalBalance.text = totalBalance.context.getString(
                R.string.usd_sign,
                state.totalPortfolioBalance.toString()
            )
        }
    }

    private fun updateChartAndAdapterData() {
        if (viewModel.state.value.chartDataLoaded) {
            val pieDataSet = PieDataSet(viewModel.state.value.chartData, "Portfolio")
            pieDataSet.setColors(Colors.pieChartColors)
            binding.pieChart.data = PieData(pieDataSet)
            binding.pieChart.invalidate()
            binding.pieChart.animate()
            adapter.addHeaderAndSubmitList(viewModel.state.value.cryptoListInUsd)
        }
    }

    private fun FragmentPortfolioBinding.setUpClickListeners() {
        depositBtn.setOnClickListener {
            openDepositDialog()
        }
        resetBalanceBtn.setOnClickListener {
            val dialog =
                ConfirmationDialogFragment(RESET_CONFIRMATION_MESSAGE, ConfirmationType.ResetBalance)
            dialog.show(parentFragmentManager, "balance_reset_dialog")
        }
    }

    private fun openDepositDialog() {
        val depositDialog = DepositDialogFragment()
        depositDialog.show(parentFragmentManager, "deposit_dialog")
    }

    private fun FragmentPortfolioBinding.setUpAdapter() {
        adapter = PortfolioListAdapter(SingleCryptoListener { slug, symbol ->
            val direction = ProfileFragmentDirections
                .actionUserProfileFragmentToCryptoItemFragment(slug, symbol)
            navController.navigate(direction)
        })
        portfolioList.adapter = adapter
    }

    private fun FragmentPortfolioBinding.updateMessageVisibility(list: List<Crypto>) {
        emptyListMessage.visibility = when (list) {
            emptyList<Crypto>() -> View.VISIBLE
            else -> View.GONE
        }
    }

    companion object {
        private val RESET_CONFIRMATION_MESSAGE = "Are you sure you want to reset balance?"
        private val DEPOSIT_CONFIRMATION_MESSAGE = "Are you sure you want to deposit?"
    }
}

