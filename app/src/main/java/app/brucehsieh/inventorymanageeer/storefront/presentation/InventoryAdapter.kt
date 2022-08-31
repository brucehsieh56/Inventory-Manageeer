package app.brucehsieh.inventorymanageeer.storefront.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.brucehsieh.inventorymanageeer.R
import app.brucehsieh.inventorymanageeer.databinding.InventoryListItemBinding
import app.brucehsieh.inventorymanageeer.common.domain.model.BaseListing
import coil.load

/**
 * [ListAdapter] to display store listings and their inventory.
 * */
class InventoryAdapter(
    private val onItemClick: (BaseListing) -> Unit,
) : ListAdapter<BaseListing, InventoryAdapter.ViewHolder>(DiffComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = InventoryListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, onItemClick)
    }

    class ViewHolder(
        private val binding: InventoryListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: BaseListing, onItemClick: (BaseListing) -> Unit) {
            binding.apply {
                textProductName.text = currentItem.productName
                textProductSku.text = currentItem.productSku
                productPrice.text =
                    itemView.context.getString(R.string.text_price, currentItem.price)

                productInventory.apply {
                    isEnabled = currentItem.quantity != -1
                    isFocusable = currentItem.quantity != -1
                    isClickable = currentItem.quantity != -1
                    setText(currentItem.quantity.toString())
                    setOnClickListener { onItemClick(currentItem) }
                }

                if (currentItem.imageUrl == null) {
                    // Reset image
                    productImage.setImageResource(R.drawable.ic_image_placeholder)
                } else {
                    productImage.load(currentItem.imageUrl) {
                        placeholder(R.drawable.ic_image_placeholder)
                        error(R.drawable.ic_image_placeholder)
                        crossfade(true)
                    }
                }
            }
        }
    }

    companion object {
        val DiffComparator = object : DiffUtil.ItemCallback<BaseListing>() {
            override fun areItemsTheSame(oldItem: BaseListing, newItem: BaseListing): Boolean {
                return oldItem.quantity == newItem.quantity
            }

            override fun areContentsTheSame(oldItem: BaseListing, newItem: BaseListing): Boolean {
                return oldItem == newItem
            }
        }
    }
}