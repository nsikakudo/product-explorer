package com.nig.productexplorer.presentation.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nig.productexplorer.R
import com.nig.productexplorer.databinding.ItemProductBinding
import com.nig.productexplorer.domain.model.Product

class ProductAdapter(
    private val onProductClick: (Int) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product, onProductClick)
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("DefaultLocale")
        fun bind(product: Product, onProductClick: (Int) -> Unit) {
            binding.apply {
                root.setOnClickListener { onProductClick(product.id) }

                productTitle.text = product.title
                productPrice.text = String.format("₦%.2f", product.price)
                productRating.rating = product.rating.toFloat()

                productImage.load(product.imageUrl) {
                    crossfade(true)
                    error(R.drawable.ic_broken_image)
                }
            }
        }
    }
}


private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
    override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
        return oldItem == newItem
    }
}