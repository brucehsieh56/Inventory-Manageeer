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
import app.brucehsieh.inventorymanageeer.data.remote.serviceapi.ShopifyApiService
import app.brucehsieh.inventorymanageeer.data.remote.serviceapi.WalmartApiService
import app.brucehsieh.inventorymanageeer.databinding.InventoryFragmentBinding
import app.brucehsieh.inventorymanageeer.ui.dialog.InventoryAdjustDialog
import app.brucehsieh.inventorymanageeer.ui.dialog.MarketKeyDialog
import app.brucehsieh.inventorymanageeer.ui.dialog.MarketKeyDialog.Companion.STORE_KEY_INT
import app.brucehsieh.inventorymanageeer.ui.store.StoreList
import app.brucehsieh.inventorymanageeer.ui.store.StorePageAdapter

private const val TAG = "InventoryFragment"

/**
 * [Fragment] inside [StorePageAdapter] for [StoreFragment].
 *
 * [InventoryFragment] will only be launch once for each page of [StorePageAdapter]. Each
 * [InventoryFragment] has its own lifecycle. When switching pages, the [InventoryFragment] will be
 * paused instead of destroyed.
 * */
class InventoryFragment : Fragment() {

    private var _binding: InventoryFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<InventoryViewModel>()

    private lateinit var marketPreferences: MarketPreferences
    private lateinit var adapter: InventoryAdapter
    private var tabPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = InventoryFragmentBinding.inflate(inflater)

        /**
         * Switch store.
         * */
        arguments?.takeIf { it.containsKey(StorePageAdapter.STORE_INDEX) }?.apply {
            tabPosition = this.getInt(StorePageAdapter.STORE_INDEX)
            viewModel.onStoreChange(tabPosition)
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
            when (it.currentStore) {
                StoreList.Walmart -> {

                    /**
                     * Return if the tab position is not matched.
                     * */
                    if (tabPosition != 0) return@observe

                    marketPreferences.walmartKeyFlow.asLiveData().distinctUntilChanged()
                        .observe(viewLifecycleOwner) { (key, secret) ->
                            if (key.isEmpty() || secret.isEmpty()) {

                                cleanUpAdapter()

                                // Launch dialog for user to enter key and secret
                                parentFragmentManager.beginTransaction()
                                    .add(
                                        MarketKeyDialog.newInstance().apply {
                                            arguments = Bundle().apply {
                                                putInt(STORE_KEY_INT, 0)
                                            }
                                        },
                                        null
                                    )
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
                StoreList.Shopify -> {

                    /**
                     * Return if the tab position is not matched.
                     * */
                    if (tabPosition != 1) return@observe

                    marketPreferences.shopifyKeyFlow.asLiveData().distinctUntilChanged()
                        .observe(viewLifecycleOwner) { (key, secret, storeName) ->
                            if (key.isEmpty() || secret.isEmpty() || storeName.isEmpty()) {
                                cleanUpAdapter()

                                // Launch dialog for user to enter key and secret
                                parentFragmentManager.beginTransaction()
                                    .add(
                                        MarketKeyDialog.newInstance().apply {
                                            arguments = Bundle().apply {
                                                putInt(STORE_KEY_INT, 1)
                                            }
                                        }, null
                                    )
                                    .commitAllowingStateLoss()
                            } else {
                                ShopifyApiService.setKey(key = key)
                                ShopifyApiService.setSecret(secret = secret)
                                ShopifyApiService.setStoreName(storeName = storeName)
                                viewModel.getShopifyItems()
                            }
                        }
                }
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

    override fun onPause() {
        super.onPause()

        /**
         * Clean up RecyclerView before switching pages to remove retained product images.
         * */
        cleanUpRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Clean up and reset [adapter] in order to remove the retained product information when
     * switching stores. Although every [InventoryFragment] page inside [StorePageAdapter] has its
     * own [adapter], the product information do not get clean up when switching pages.
     * */
    private fun cleanUpAdapter() {
        adapter.data = emptyList()
        adapter.notifyDataSetChanged()
    }

    /**
     * Clean up and reset [RecyclerView] in order to remove the retained product information when
     * switching stores.
     * */
    private fun cleanUpRecyclerView() {
        binding.listingRecyclerView.removeAllViews()
        binding.listingRecyclerView.recycledViewPool.clear()
    }
}