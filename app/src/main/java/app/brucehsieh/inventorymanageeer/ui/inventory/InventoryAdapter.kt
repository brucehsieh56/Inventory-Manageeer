package app.brucehsieh.inventorymanageeer.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.brucehsieh.inventorymanageeer.R
import app.brucehsieh.inventorymanageeer.databinding.InventoryListItemBinding
import app.brucehsieh.inventorymanageeer.model.BaseListing

private const val TAG = "InventoryAdapter"

/**
 * [ListAdapter] to display store listings.
 * */
class InventoryAdapter(
    private val onItemClick: (BaseListing) -> Unit
) : ListAdapter<BaseListing, InventoryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private val binding: InventoryListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            currentItem: BaseListing,
            onInventoryClick: (BaseListing) -> Unit
        ) {
            binding.apply {
                productName.text = currentItem.productName
                productSku.text = currentItem.productSku
                productPrice.text =
                    itemView.context.getString(R.string.text_price, currentItem.price)

                productInventory.apply {
                    isEnabled = currentItem.quantity != -1
                    isFocusable = currentItem.quantity != -1
                    isClickable = currentItem.quantity != -1
                    setText(currentItem.quantity.toString())
                    setOnClickListener {
                        onInventoryClick(currentItem)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(InventoryListItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, onItemClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BaseListing>() {
        override fun areItemsTheSame(oldItem: BaseListing, newItem: BaseListing): Boolean {
            return oldItem.productName == newItem.productName
        }

        override fun areContentsTheSame(oldItem: BaseListing, newItem: BaseListing): Boolean {
            return oldItem == newItem
        }
    }
}