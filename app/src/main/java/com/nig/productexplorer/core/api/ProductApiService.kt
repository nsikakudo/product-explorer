package com.nig.productexplorer.core.api

import com.nig.productexplorer.data.remote.ProductRemoteEntity
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApiService {
    @GET("products")
    suspend fun getAllProducts(): List<ProductRemoteEntity>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: Int): ProductRemoteEntity
}
