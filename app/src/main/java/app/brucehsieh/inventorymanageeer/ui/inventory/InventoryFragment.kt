package app.brucehsieh.inventorymanageeer.ui.inventory

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import app.brucehsieh.inventorymanageeer.data.MarketPreferences
import app.brucehsieh.inventorymanageeer.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.databinding.InventoryFragmentBinding
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog
import app.brucehsieh.inventorymanageeer.ui.dialog.MarketKeyDialog
import app.brucehsieh.inventorymanageeer.ui.store.StoreList
import app.brucehsieh.inventorymanageeer.ui.store.StorePageAdapter

private const val TAG = "InventoryFragment"

class InventoryFragment : Fragment() {

    private var _binding: InventoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<InventoryViewModel>()

    private lateinit var marketPreferences: MarketPreferences
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

        marketPreferences = MarketPreferences(requireContext())

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

        viewModel.inventoryViewState.observe(viewLifecycleOwner) {

            /**
             * Clean up and reset [RecyclerView] in order to remove the retained product images when
             * switching stores.
             * */
            binding.listingRecyclerView.removeAllViews()
            binding.listingRecyclerView.recycledViewPool.clear()

            when (it.currentStore) {
                StoreList.Walmart -> {
                    marketPreferences.walmartKeyFlow.asLiveData().distinctUntilChanged()
                        .observe(viewLifecycleOwner) { (key, secret) ->
                            if (key.isEmpty() || secret.isEmpty()) {
                                // Launch dialog for user to enter key and secret
                                parentFragmentManager.beginTransaction()
                                    .add(MarketKeyDialog.newInstance(), null)
                                    .commitAllowingStateLoss()
                            } else {
                                // Load key and secret
                                WalmartApiService.setKey(key = key)
                                WalmartApiService.setSecret(secret = secret)

                                // Run request
                                viewModel.getWalmartItems()
                            }
                        }
                }
                StoreList.Shopify -> viewModel.getShopifyItems()
            }
        }

        viewModel.productListings.observe(viewLifecycleOwner) {

            it ?: return@observe

            if (it.isNotEmpty()) {
                Log.i(TAG, "onViewCreated: ${it.size}")
                adapter.data = it
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}