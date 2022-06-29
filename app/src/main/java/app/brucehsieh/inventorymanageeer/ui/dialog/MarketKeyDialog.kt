package app.brucehsieh.inventorymanageeer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import app.brucehsieh.inventorymanageeer.data.MarketPreferences
import app.brucehsieh.inventorymanageeer.databinding.KeyInputDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [DialogFragment] for user to enter or modify marketplace key.
 * */
class MarketKeyDialog : DialogFragment() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)

    private var _binding: KeyInputDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var marketPreferences: MarketPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = KeyInputDialogBinding.inflate(LayoutInflater.from(context))

        marketPreferences = MarketPreferences(requireContext())

        binding.apply {

            cancelButton.setOnClickListener { dismissAllowingStateLoss() }

            updateKeyValueButton.setOnClickListener {
                coroutineScope.launch {
                    // Update key and secret
                    val key = keyTextInputLayout.editText?.text.toString()
                    val secret = secretTextInputLayout.editText?.text.toString()
                    marketPreferences.storeWalmartKey(
                        key = key,
                        secret = secret,
                        context = requireContext()
                    )

                    dismissAllowingStateLoss()
                }
            }

            coroutineScope.launch {
                // Read saved key and secret, and display them to the editTexts
                val (key, secret) = marketPreferences.walmartKeyFlow.first()
                keyTextInputLayout.editText?.setText(key)
                secretTextInputLayout.editText?.setText(secret)
            }
        }

        return MaterialAlertDialogBuilder(requireContext()).setView(binding.root).create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.coroutineContext.cancel()
        _binding = null
    }

    companion object {
        fun newInstance(): MarketKeyDialog {
            return MarketKeyDialog()
        }
    }
}