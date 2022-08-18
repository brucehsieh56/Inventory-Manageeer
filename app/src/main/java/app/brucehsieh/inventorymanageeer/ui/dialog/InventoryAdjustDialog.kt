//package app.brucehsieh.inventorymanageeer.ui.dialog
//
//import android.app.Dialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import androidx.core.widget.doOnTextChanged
//import androidx.fragment.app.DialogFragment
//import androidx.fragment.app.activityViewModels
//import app.brucehsieh.inventorymanageeer.common.extension.ShortSnackbar
//import app.brucehsieh.inventorymanageeer.databinding.InventoryAdjustDialogBinding
//import app.brucehsieh.inventorymanageeer.common.domain.model.BaseListing
//import app.brucehsieh.inventorymanageeer.common.domain.model.ShopifyListing
//import app.brucehsieh.inventorymanageeer.ui.inventory.InventoryViewModel
//import app.brucehsieh.inventorymanageeer.storefront.domain.StoreList
//import com.google.android.material.dialog.MaterialAlertDialogBuilder
//
//private const val TAG = "InventoryAdjustDialog"
//
///**
// * A [DialogFragment] for user to adjust inventory.
// * */
//class InventoryAdjustDialog : DialogFragment() {
//
//    private var _binding: InventoryAdjustDialogBinding? = null
//    private val binding get() = _binding!!
//    private val viewModel by activityViewModels<InventoryViewModel>()
//
//    private var newQuantity = 0
//    private lateinit var currentListing: BaseListing
//    private lateinit var currentMarketplace: StoreList
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        _binding = InventoryAdjustDialogBinding.inflate(LayoutInflater.from(context))
//        currentListing = viewModel.currentSelectedListing!!
//
//        /**
//         * Get current marketplace from [getArguments].
//         * */
//        currentMarketplace = when (arguments?.getInt(MarketKeyDialog.STORE_KEY_INT)) {
//            0 -> StoreList.Walmart
//            1 -> StoreList.Shopify
//            2 -> StoreList.Walmart
//            else -> throw IllegalArgumentException("Invalid marketplace.")
//        }
//
//        setUpUI()
//        setUpListeners()
//
//        return MaterialAlertDialogBuilder(requireContext()).setView(binding.root).create()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun setUpUI() {
//        binding.apply {
//            quantitySlider.value = currentListing.quantity.toFloat().takeIf { it > 0f } ?: 0f
//
//            // Update quantity
//            newQuantity = binding.quantitySlider.value.toInt()
//
//            productName.text = currentListing.productName
//            productSku.text = currentListing.productSku
//            productQuantity.setText(newQuantity.toString())
//        }
//    }
//
//    private fun setUpListeners() {
//        binding.apply {
//            productQuantity.doOnTextChanged { text, start, before, count ->
//                try {
//                    val number = when {
//                        // If the input digit is empty or blank, set the number to 0
//                        text.toString().isBlank() -> 0
//                        else -> text.toString().toInt()
//                    }
//
//                    // Avoid recursive call to [doOnTextChanged].
//                    when {
//                        // Update when input text is different than the previous one
//                        number != newQuantity -> updateSliderAndEditText(number)
//                        // Update when input text has 00...
//                        number == 0 && text.toString() == "00" -> updateSliderAndEditText(number)
//                    }
//                } catch (t: Throwable) {
//                    t.printStackTrace()
//                }
//            }
//
//            quantitySlider.addOnChangeListener { slider, value, fromUser ->
//                newQuantity = value.toInt()
//                productQuantity.setText(newQuantity.toString())
//            }
//
//            updateQuantityButton.setOnClickListener {
//                // Check current store
//                when (currentMarketplace) {
//                    StoreList.Walmart -> {
//                        viewModel.updateInventoryBySku(
//                            sku = viewModel.currentSelectedListing!!.productSku,
//                            newQuantity = newQuantity
//                        )
//                    }
//                    StoreList.Shopify -> {
//                        viewModel.updateShopifyInventory(
//                            inventoryItemId = (viewModel.currentSelectedListing as ShopifyListing).inventoryItemId!!,
//                            newQuantity = newQuantity
//                        )
//                    }
//                    else -> Unit
//                }
//
//                dismissAllowingStateLoss()
//            }
//
//            cancelButton.setOnClickListener { dismissAllowingStateLoss() }
//        }
//    }
//
//    /**
//     * Sync [Slider] and [EditText] when changing [EditText]. Also pose upper and lower bounds to
//     * the inventory.
//     * */
//    private fun updateSliderAndEditText(number: Int) {
//
//        newQuantity = when {
//            number > 500 -> {
//                parentFragment?.requireView()?.ShortSnackbar("Maximum number of inventory is 500.")
//                500
//            }
//            number < 0 -> {
//                parentFragment?.requireView()?.ShortSnackbar("Minimum number of inventory is 0.")
//                0
//            }
//            else -> {
//                number
//            }
//        }
//
//        binding.apply {
//            productQuantity.postDelayed(
//                {
//                    // Update [EditText] text
//                    productQuantity.setText(newQuantity.toString())
//
//                    // Update [Slider]
//                    quantitySlider.value = newQuantity.toFloat()
//
//                    // Update [EditText] cursor position
//                    productQuantity.setSelection(productQuantity.length())
//                },
//                100
//            )
//        }
//    }
//
//    companion object {
//        fun newInstance(): InventoryAdjustDialog {
//            return InventoryAdjustDialog()
//        }
//    }
//}