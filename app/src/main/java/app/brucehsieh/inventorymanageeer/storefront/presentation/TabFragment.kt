package app.brucehsieh.inventorymanageeer.storefront.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.brucehsieh.inventorymanageeer.R
import app.brucehsieh.inventorymanageeer.databinding.FragmentTabBinding
import app.brucehsieh.inventorymanageeer.storefront.domain.StoreList
import app.brucehsieh.inventorymanageeer.storefront.presentation.ShopifyInventoryFragment
import app.brucehsieh.inventorymanageeer.storefront.presentation.WalmartInventoryFragment
import com.google.android.material.tabs.TabLayout

private const val KEY_SELECTED_INDEX = "KEY_SELECTED_INDEX"

/**
 * Fragment to handle tab layout and display sub fragment.
 * */
class TabFragment : Fragment() {

    private var _binding: FragmentTabBinding? = null
    private val binding get() = _binding!!

    private var selectedIndex = 0
    private val fragmentDict = hashMapOf<Int, Fragment>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentTabBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            // Add first fragment
            fragmentDict[selectedIndex] = WalmartInventoryFragment().apply {
                arguments = Bundle().apply {
                    putInt(STORE_INDEX, selectedIndex)
                }
            }
            parentFragmentManager.beginTransaction()
                .add(
                    R.id.fragment_container,
                    fragmentDict[selectedIndex]!!,
                    fragmentDict.size.toString()
                )
                .commit()
        } else {
            selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
        }
        selectFragment(fragmentDict[selectedIndex]!!)

        inflateTabLayout()
        binding.tabLayout.addOnTabSelectedListener(tabSelectedListener())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Display the selected fragment.
     * */
    private fun selectFragment(selectedFragment: Fragment) {
        var transaction = parentFragmentManager.beginTransaction()
        fragmentDict.forEach { (k, v) ->
            if (selectedFragment == v) {
                transaction = transaction.attach(v)
                selectedIndex = k
            } else {
                transaction = transaction.detach(v)
            }
        }
        transaction.commit()
    }

    /**
     * Set up [TabLayout] and render texts.
     * */
    private fun inflateTabLayout() {
        with(binding.tabLayout) {
            StoreList.values().forEach { store ->
                addTab(newTab().setText(store.name))
            }
        }
    }


    /**
     * Listener to handle tab click events.
     * */
    private fun tabSelectedListener(): TabLayout.OnTabSelectedListener {
        return object : TabLayout.OnTabSelectedListener {
            val fragmentManager = parentFragmentManager

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    selectedIndex = position

                    // Add new fragment if it is not exist yet
                    if (!fragmentDict.containsKey(selectedIndex)) {

                        val fragment = if (selectedIndex == 1) ShopifyInventoryFragment()
                        else WalmartInventoryFragment()

                        fragmentDict[selectedIndex] = fragment.apply {
                            arguments = Bundle().apply { putInt(STORE_INDEX, selectedIndex) }
                        }
                        fragmentManager.beginTransaction()
                            .add(
                                R.id.fragment_container,
                                fragmentDict[selectedIndex]!!,
                                fragmentDict.size.toString()
                            )
                            .commit()
                    }
                    selectFragment(fragmentDict[selectedIndex]!!)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        }
    }

    companion object {
        const val STORE_INDEX = "STORE_INDEX"
    }
}