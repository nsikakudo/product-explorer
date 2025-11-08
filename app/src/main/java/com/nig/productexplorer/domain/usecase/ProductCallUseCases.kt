package com.nig.productexplorer.domain.usecase

import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.data.repository.ProductRepository
import com.nig.productexplorer.domain.model.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class ProductCallUseCases @Inject constructor(
    private val repository: ProductRepository
) {
    fun getAllProducts(): Flow<Resource<List<Product>>> {
        return repository.getProducts()
    }

    fun getProductDetail(productId: Int): Flow<Resource<Product>> {
        return repository.getProductDetail(productId)
    }
}