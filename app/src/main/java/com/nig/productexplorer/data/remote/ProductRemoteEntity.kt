package com.nig.productexplorer.data.remote

data class ProductRemoteEntity(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: NetworkRating
)

data class NetworkRating(
    val rate: Double,
    val count: Int
)
