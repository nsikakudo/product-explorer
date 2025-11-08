package com.nig.productexplorer.core.api

import com.nig.productexplorer.data.remote.ProductRemoteEntity

interface ProductApiHelper {
    suspend fun getAllProducts(): List<ProductRemoteEntity>
    suspend fun getProductById(id: Int): ProductRemoteEntity
}