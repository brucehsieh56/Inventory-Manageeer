package app.brucehsieh.inventorymanageeer.storefront.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import app.brucehsieh.inventorymanageeer.common.domain.model.BaseListing
import app.brucehsieh.inventorymanageeer.common.presentation.OneTimeEvent
import app.brucehsieh.inventorymanageeer.databinding.FragmentInventoryBinding
import app.brucehsieh.inventorymanageeer.storefront.presentation.TabFragment.Companion.STORE_INDEX
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog.Companion.TO_DIALOG_PRODUCT_NAME_KEY
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog.Companion.TO_DIALOG_QUANTITY_KEY
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog.Companion.TO_DIALOG_SKU_KEY
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog.Companion.TO_FRAGMENT_QUANTITY_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalmartInventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<WalmartViewModel>()

    private var tabPosition: Int = -1
    private lateinit var inventoryAdapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater)

        /**
         * Get [tabPosition].
         * */
        arguments?.takeIf { it.containsKey(STORE_INDEX) }?.apply {
            tabPosition = this.getInt(STORE_INDEX)
        }

        /**
         * Listen to [InventoryAdjustDialog] for inventory update
         * */
        setFragmentResultListener(TO_FRAGMENT_QUANTITY_KEY) { _, bundle ->
            val newQuantity = bundle.getInt(TO_FRAGMENT_QUANTITY_KEY)
            val productSku = bundle.getString(InventoryAdjustDialog.TO_FRAGMENT_SKU_KEY)!!
            viewModel.onInventoryUpdateBySku(sku = productSku, newQuantity = newQuantity)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventoryAdapter = InventoryAdapter(::launchInventoryDialog)

        binding.listingRecyclerView.adapter = inventoryAdapter
        binding.swipeRefreshLayout.setOnRefreshListener { viewModel.onListingLoad() }

        viewModel.uiState.observe(viewLifecycleOwner) {
            inventoryAdapter.submitList(it.listings)
            handleLoading(it.isLoading)
            handleFailure(it.error)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleLoading(isLoading: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = isLoading
    }

    private fun handleFailure(error: OneTimeEvent<Throwable>?) {
        val unHandled = error?.getContentIfNotHandled() ?: return
        unHandled.printStackTrace()
    }

    /**
     * Launch [InventoryAdjustDialog] for inventory adjust.
     * */
    private fun launchInventoryDialog(listing: BaseListing) {
        parentFragmentManager.beginTransaction()
            .add(
                InventoryAdjustDialog().apply {
                    arguments = bundleOf(
                        TO_DIALOG_QUANTITY_KEY to listing.quantity,
                        TO_DIALOG_SKU_KEY to listing.productSku,
                        TO_DIALOG_PRODUCT_NAME_KEY to listing.productName
                    )
                },
                null
            )
            .commitAllowingStateLoss()
    }
}