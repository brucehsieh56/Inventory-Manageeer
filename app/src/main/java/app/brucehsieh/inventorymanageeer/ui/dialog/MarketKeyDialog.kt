package app.brucehsieh.inventorymanageeer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import app.brucehsieh.inventorymanageeer.data.MarketPreferences
import app.brucehsieh.inventorymanageeer.databinding.KeyInputDialogBinding
import app.brucehsieh.inventorymanageeer.ui.inventory.InventoryViewModel
import app.brucehsieh.inventorymanageeer.ui.store.StoreList
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
    private val viewModel by activityViewModels<InventoryViewModel>()

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

                    when (viewModel.inventoryViewState.value?.currentStore) {
                        StoreList.Walmart -> {
                            marketPreferences.storeWalmartKey(
                                key = key,
                                secret = secret,
                                context = requireContext()
                            )
                        }
                        StoreList.Shopify -> {
                            val storeName = storeNameTextInputLayout.editText?.text.toString()

                            marketPreferences.storeShopifyKey(
                                key = key,
                                secret = secret,
                                storeName = storeName,
                                context = requireContext()
                            )
                        }
                        null -> Unit
                    }

                    dismissAllowingStateLoss()
                }
            }

            coroutineScope.launch {
                // Read saved key and secret, and display them to the editTexts
                when (viewModel.inventoryViewState.value?.currentStore) {
                    StoreList.Walmart -> {
                        storeNameTextInputLayout.visibility = View.INVISIBLE
                        val (key, secret) = marketPreferences.walmartKeyFlow.first()
                        keyTextInputLayout.editText?.setText(key)
                        secretTextInputLayout.editText?.setText(secret)
                    }
                    StoreList.Shopify -> {
                        storeNameTextInputLayout.visibility = View.VISIBLE
                        val (key, secret, storeName) = marketPreferences.shopifyKeyFlow.first()
                        keyTextInputLayout.editText?.setText(key)
                        secretTextInputLayout.editText?.setText(secret)
                        storeNameTextInputLayout.editText?.setText(storeName)
                    }
                    null -> Unit
                }
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