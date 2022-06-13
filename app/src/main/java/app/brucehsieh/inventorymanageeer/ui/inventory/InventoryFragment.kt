package app.brucehsieh.inventorymanageeer.ui.inventory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.brucehsieh.inventorymanageeer.databinding.InventoryFragmentBinding
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog
import app.brucehsieh.inventorymanageeer.ui.store.StorePageAdapter

private const val TAG = "InventoryFragment"

class InventoryFragment : Fragment() {

    private var _binding: InventoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<InventoryViewModel>()

    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = InventoryFragmentBinding.inflate(inflater)

        /**
         * Switch store.
         * */
        arguments?.takeIf { it.containsKey(StorePageAdapter.STORE_INDEX) }?.apply {
            viewModel.onStoreChange(this.getInt(StorePageAdapter.STORE_INDEX))
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InventoryAdapter { listing ->

            // Cache current selected
            viewModel.updateCurrentSelected(listing)

            // Launch dialog
            parentFragmentManager.beginTransaction()
                .add(InventoryAdjustDialog.newInstance(), null)
                .commitAllowingStateLoss()
        }

        binding.listingRecyclerView.adapter = adapter

        /**
         * Set to null to avoid crash
         * */
        binding.listingRecyclerView.itemAnimator = null

        viewModel.productListings.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    Log.i(TAG, "onViewCreated: ${it.size}")
                    adapter.submitList(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}