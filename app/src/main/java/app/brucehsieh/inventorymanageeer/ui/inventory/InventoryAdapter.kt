package app.brucehsieh.inventorymanageeer.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.brucehsieh.inventorymanageeer.R
import app.brucehsieh.inventorymanageeer.databinding.InventoryListItemBinding
import app.brucehsieh.inventorymanageeer.model.BaseListing
import coil.load

private const val TAG = "InventoryAdapter"

/**
 * [ListAdapter] to display store listings.
 * */
class InventoryAdapter(
    private val onItemClick: (BaseListing) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

    var data: List<BaseListing> = emptyList()

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

                if (currentItem.imageUrl == null) {
                    // Reset image
                    productImage.setImageResource(R.drawable.ic_image_placeholder)
                } else {
                    productImage.load(currentItem.imageUrl) {
                        crossfade(true)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(InventoryListItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (data.isNotEmpty()) {
            val currentItem = data[position]
            holder.bind(currentItem, onItemClick)
        }
    }
}