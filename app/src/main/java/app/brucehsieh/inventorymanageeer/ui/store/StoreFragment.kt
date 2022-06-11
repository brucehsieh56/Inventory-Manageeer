package app.brucehsieh.inventorymanageeer.ui.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.brucehsieh.inventorymanageeer.R
import app.brucehsieh.inventorymanageeer.databinding.StoreFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * [Fragment] that contains [TabLayout] and [ViewPager2]. [ViewPager2] displays [InventoryFragment].
 * */
class StoreFragment : Fragment() {

    private var _binding: StoreFragmentBinding? = null
    private val binding get() = _binding!!

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}