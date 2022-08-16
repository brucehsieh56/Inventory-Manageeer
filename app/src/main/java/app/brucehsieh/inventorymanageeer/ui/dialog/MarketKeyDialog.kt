package app.brucehsieh.inventorymanageeer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import app.brucehsieh.inventorymanageeer.R
import app.brucehsieh.inventorymanageeer.common.data.MarketPreferences
import app.brucehsieh.inventorymanageeer.databinding.KeyInputDialogBinding
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

    private lateinit var marketPreferences: MarketPreferences
    private lateinit var currentMarketplace: StoreList

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        _binding = KeyInputDialogBinding.inflate(LayoutInflater.from(context))
        marketPreferences = MarketPreferences(requireContext())

        /**
         * Get current marketplace from [getArguments].
         * */
        currentMarketplace = when (arguments?.getInt(STORE_KEY_INT)) {
            0 -> StoreList.Walmart
            1 -> StoreList.Shopify
            else -> throw IllegalArgumentException("Invalid marketplace.")
        }

        setUpUI()
        setUpListeners()

        return MaterialAlertDialogBuilder(requireContext()).setView(binding.root).create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.coroutineContext.cancel()
        _binding = null
    }

    private fun setUpUI() {
        binding.apply {
            coroutineScope.launch {
                // Read saved key and secret, and display them to the editTexts
                when (currentMarketplace) {
                    StoreList.Walmart -> {
                        storeNameTextInputLayout.visibility = View.INVISIBLE
                        val (key, secret) = marketPreferences.walmartKeyFlow.first()
                        keyTextInputLayout.editText?.setText(key)
                        secretTextInputLayout.editText?.setText(secret)

                        keyDialogDescription.text = getString(
                            R.string.dialog_key_description_walmart,
                            currentMarketplace.name
                        )
                    }
                    StoreList.Shopify -> {
                        storeNameTextInputLayout.visibility = View.VISIBLE
                        val (key, secret, storeName) = marketPreferences.shopifyKeyFlow.first()
                        keyTextInputLayout.editText?.setText(key)
                        secretTextInputLayout.editText?.setText(secret)
                        storeNameTextInputLayout.editText?.setText(storeName)

                        keyDialogDescription.text = getString(
                            R.string.dialog_key_description_shopify,
                            currentMarketplace.name
                        )
                    }
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.apply {
            cancelButton.setOnClickListener { dismissAllowingStateLoss() }

            updateKeyValueButton.setOnClickListener {
                coroutineScope.launch {
                    // Update key and secret
                    val key = keyTextInputLayout.editText?.text.toString()
                    val secret = secretTextInputLayout.editText?.text.toString()

                    when (currentMarketplace) {
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
                    }

                    dismissAllowingStateLoss()
                }
            }
        }
    }

    companion object {

        const val STORE_KEY_INT = "STORE_KEY_INT"

        fun newInstance(): MarketKeyDialog {
            return MarketKeyDialog()
        }
    }
}