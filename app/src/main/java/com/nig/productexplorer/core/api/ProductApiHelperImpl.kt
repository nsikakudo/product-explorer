package com.nig.productexplorer.core.api

import com.nig.productexplorer.data.remote.ProductRemoteEntity
import javax.inject.Inject

class ProductApiHelperImpl @Inject constructor(
    private val apiService: ProductApiService
) : ProductApiHelper {

    override suspend fun getAllProducts(): List<ProductRemoteEntity> {
        return apiService.getAllProducts()
    }
    override suspend fun getProductById(id: Int): ProductRemoteEntity {
        return apiService.getProductDetail(id)
    }
}