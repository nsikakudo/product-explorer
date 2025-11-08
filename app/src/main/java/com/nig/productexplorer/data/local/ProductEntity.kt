package com.nig.productexplorer.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nig.productexplorer.data.remote.ProductRemoteEntity

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    @Embedded(prefix = "rating_")
    val rating: Rating,
    val lastFetched: Long = System.currentTimeMillis()
)

data class Rating(
    val rate: Double,
    val count: Int
)


fun ProductRemoteEntity.toDBEntity(): ProductEntity {
    return ProductEntity(
        id = this.id,
        title = this.title,
        price = this.price,
        description = this.description,
        category = this.category,
        image = this.image,
        rating = Rating(this.rating.rate, this.rating.count),
        lastFetched = System.currentTimeMillis()
    )
}
