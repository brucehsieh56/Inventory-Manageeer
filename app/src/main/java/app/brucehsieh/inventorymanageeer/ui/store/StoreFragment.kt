package app.brucehsieh.inventorymanageeer.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.brucehsieh.inventorymanageeer.databinding.StoreFragmentBinding
import app.brucehsieh.inventorymanageeer.ui.inventory.InventoryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * [Fragment] that contains [TabLayout] and [ViewPager2]. [ViewPager2] displays [InventoryFragment].
 * */
class StoreFragment : Fragment() {

    private var _binding: StoreFragmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<InventoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StoreFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up ViewPager and TabLayout
        binding.viewPager.adapter = StorePageAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = StoreList.values()[position].name
            tab.setIcon(StoreList.getIconDrawable(StoreList.values()[position]))
        }.attach()

        // Set up listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                // Notify viewModel to change store
                tab?.position?.let {
                    viewModel.onStoreChange(it)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // TODO: Scroll to top and force to refresh
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}