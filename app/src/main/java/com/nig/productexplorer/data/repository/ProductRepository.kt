package com.nig.productexplorer.data.repository

import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Resource<List<Product>>>
    fun getProductDetail(productId: Int): Flow<Resource<Product>>
}