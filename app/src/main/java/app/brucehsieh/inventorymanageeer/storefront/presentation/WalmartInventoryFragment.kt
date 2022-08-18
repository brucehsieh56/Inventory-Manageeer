package app.brucehsieh.inventorymanageeer.storefront.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.brucehsieh.inventorymanageeer.databinding.FragmentInventoryBinding
import app.brucehsieh.inventorymanageeer.storefront.presentation.TabFragment.Companion.STORE_INDEX

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onListingLoad()

        inventoryAdapter = InventoryAdapter {
            // Cache current selected
            // Launch dialog
        }

        binding.listingRecyclerView.adapter = inventoryAdapter

        viewModel.uiState.observe(viewLifecycleOwner) {
            inventoryAdapter.submitList(it.listings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}