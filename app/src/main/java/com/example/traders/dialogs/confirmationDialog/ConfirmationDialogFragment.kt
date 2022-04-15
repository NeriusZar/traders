package com.example.traders.dialogs.confirmationDialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.traders.databinding.DialogConfirmationBinding
import com.example.traders.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ConfirmationDialogFragment(private val message: String) : DialogFragment() {
    private lateinit var binding: DialogConfirmationBinding
    val viewModel: ConfirmationDialogViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            binding = DialogConfirmationBinding.inflate(inflater)
            binding.confirmationMsg.text = message
            binding.setClickListeners()
            val dialog = builder.setView(binding.root)
                .setCancelable(false)
                .create()

            lifecycleScope.launchWhenCreated {
                viewModel.events.collect { event ->
                    when (event) {
                        ConfirmationDialogEvent.Dismiss -> {
                            DismissDialog()
                        }
                    }.exhaustive
                }
            }

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun DialogConfirmationBinding.setClickListeners() {
        successBtn.setOnClickListener {
            viewModel.onAcceptButtonClicked()
        }

        cancelBtn.setOnClickListener {
            viewModel.onCancelButtonClicked()
        }
    }

    private fun DismissDialog() {
        dialog?.dismiss()
    }

}
