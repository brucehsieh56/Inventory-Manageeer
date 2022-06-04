package app.brucehsieh.inventorymanageeer.ui.inventory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import app.brucehsieh.inventorymanageeer.databinding.InventoryFragmentBinding

private const val TAG = "InventoryFragment"

class InventoryFragment : Fragment() {

    private var _binding: InventoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<InventoryViewModel>()

    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = InventoryFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InventoryAdapter()

        binding.listingRecyclerView.adapter = adapter

        viewModel.walmartListing.observe(viewLifecycleOwner) {
            Log.i(TAG, "onViewCreated: ${it.size}")
            adapter.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}