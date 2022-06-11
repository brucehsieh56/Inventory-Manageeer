package app.brucehsieh.inventorymanageeer.ui.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import app.brucehsieh.inventorymanageeer.ui.inventory.InventoryFragment

/**
 * [FragmentStateAdapter] to switch market place.
 * */
class StorePageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = StoreList.values().size

    override fun createFragment(position: Int): Fragment {
        return InventoryFragment().apply {
            arguments = Bundle().apply {
                putInt(STORE_INDEX, position)
            }
        }
    }

    companion object {
        const val STORE_INDEX = "STORE_INDEX"
    }
}
