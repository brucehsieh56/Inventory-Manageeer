package app.brucehsieh.inventorymanageeer.ui.store

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import app.brucehsieh.inventorymanageeer.R
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
        setHasOptionsMenu(true)
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

        /**
         * Set up chip listener. Click to change the sorting method.
         * */
        binding.sortingChipGroup1.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                1 -> viewModel.sorting(isAscending = true)
                2 -> viewModel.sorting(isAscending = false)
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.store_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sorting_action -> {
                when (binding.sortingChipGroup1.visibility) {
                    View.GONE -> binding.sortingChipGroup1.visibility = View.VISIBLE
                    else -> binding.sortingChipGroup1.visibility = View.GONE
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}