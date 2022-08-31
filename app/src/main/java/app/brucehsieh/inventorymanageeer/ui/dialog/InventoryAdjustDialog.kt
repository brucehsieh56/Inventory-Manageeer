package app.brucehsieh.inventorymanageeer.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import app.brucehsieh.inventorymanageeer.common.extension.ShortSnackbar
import app.brucehsieh.inventorymanageeer.common.extension.empty
import app.brucehsieh.inventorymanageeer.databinding.InventoryAdjustDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * [DialogFragment] for users to adjust inventory.
 *
 * The listing data is passed
 *  - via [setFragmentResult] with [Bundle] from [InventoryAdjustDialog] to [Fragment].
 *  - by [getArguments] with [Bundle] from [Fragment] to [InventoryAdjustDialog].
 * */
class InventoryAdjustDialog : DialogFragment() {
    companion object {
        const val TO_DIALOG_QUANTITY_KEY = "TO_DIALOG_QUANTITY_KEY"
        const val TO_DIALOG_SKU_KEY = "TO_DIALOG_SKU_KEY"
        const val TO_DIALOG_PRODUCT_NAME_KEY = "TO_DIALOG_PRODUCT_NAME_KEY"
        const val TO_DIALOG_PRODUCT_ID = "TO_DIALOG_PRODUCT_ID"

        const val TO_FRAGMENT_QUANTITY_KEY = "TO_FRAGMENT_QUANTITY_KEY"
        const val TO_FRAGMENT_SKU_KEY = "TO_FRAGMENT_SKU_KEY"
        const val TO_FRAGMENT_ID_KEY = "TO_FRAGMENT_ID_KEY"
    }

    private var _binding: InventoryAdjustDialogBinding? = null
    private val binding get() = _binding!!

    private var quantity: Int = -1
    private var productId: Long = -1
    private lateinit var productSku: String
    private lateinit var productName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            quantity = getInt(TO_DIALOG_QUANTITY_KEY)
            productId = getLong(TO_DIALOG_PRODUCT_ID)
            productSku = getString(TO_DIALOG_SKU_KEY) ?: String.empty()
            productName = getString(TO_DIALOG_PRODUCT_NAME_KEY)
                ?: throw IllegalArgumentException("Invalid product name")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = InventoryAdjustDialogBinding.inflate(LayoutInflater.from(context))

        setUpUI()
        setUpListeners()

        return MaterialAlertDialogBuilder(requireContext()).setView(binding.root).create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpUI() {
        binding.apply {
            quantitySlider.value = quantity.toFloat().takeIf { it > 0f } ?: 0f
            textProductName.text = productName
            textProductSku.text = productSku
            productQuantity.setText(quantity.toString())
        }
    }

    private fun setUpListeners() {
        binding.apply {
            productQuantity.doOnTextChanged { text, start, before, count ->
                try {
                    val number = when {
                        // If the input digit is empty or blank, set the number to 0
                        text.toString().isBlank() -> 0
                        else -> text.toString().toInt()
                    }

                    // Avoid recursive call to [doOnTextChanged].
                    when {
                        // Update when input text is different than the previous one
                        number != quantity -> updateSliderAndEditText(number)
                        // Update when input text has 00...
                        number == 0 && text.toString() == "00" -> updateSliderAndEditText(number)
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }

            quantitySlider.addOnChangeListener { slider, value, fromUser ->
                quantity = value.toInt()
                productQuantity.setText(quantity.toString())
            }

            updateQuantityButton.setOnClickListener {
                setFragmentResult(TO_FRAGMENT_QUANTITY_KEY,
                    bundleOf(
                        TO_FRAGMENT_QUANTITY_KEY to quantity,
                        TO_FRAGMENT_SKU_KEY to productSku,
                        TO_FRAGMENT_ID_KEY to productId
                    )
                )

                dismissAllowingStateLoss()
            }

            cancelButton.setOnClickListener { dismissAllowingStateLoss() }
        }
    }

    /**
     * Sync [Slider] and [EditText] when changing [EditText]. Also pose upper and lower bounds to
     * the inventory.
     * */
    private fun updateSliderAndEditText(number: Int) {

        quantity = when {
            number > 500 -> {
                parentFragment?.requireView()?.ShortSnackbar("Maximum number of inventory is 500.")
                500
            }
            number < 0 -> {
                parentFragment?.requireView()?.ShortSnackbar("Minimum number of inventory is 0.")
                0
            }
            else -> {
                number
            }
        }

        binding.apply {
            productQuantity.postDelayed(
                {
                    // Update [EditText] text
                    productQuantity.setText(quantity.toString())

                    // Update [Slider]
                    quantitySlider.value = quantity.toFloat()

                    // Update [EditText] cursor position
                    productQuantity.setSelection(productQuantity.length())
                },
                100
            )
        }
    }
}
