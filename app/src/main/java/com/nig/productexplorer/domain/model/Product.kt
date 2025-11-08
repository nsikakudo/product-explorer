package com.nig.productexplorer.domain.model

import com.nig.productexplorer.data.local.ProductEntity

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val imageUrl: String,
    val rating: Double,
    val ratingCount: Int
)

fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        title = title,
        price = price,
        description = description,
        category = category,
        imageUrl = image,
        rating = rating.rate,
        ratingCount = rating.count
    )
}
