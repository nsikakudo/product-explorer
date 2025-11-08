package com.nig.productexplorer.data.repository

import com.nig.productexplorer.core.api.ProductApiHelper
import com.nig.productexplorer.core.di.AppModule
import com.nig.productexplorer.core.network.NetworkMonitor
import com.nig.productexplorer.core.network.safeApiCall
import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.data.local.ProductDao
import com.nig.productexplorer.data.local.toDBEntity
import com.nig.productexplorer.domain.model.Product
import com.nig.productexplorer.domain.model.toDomain
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productApiHelper: ProductApiHelper,
    private val productDao: ProductDao,
    private val networkMonitor: NetworkMonitor,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    override fun getProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())

        val initialCachedList = productDao.getAllProducts().first()
        val hasCache = initialCachedList.isNotEmpty()

        if (hasCache) {
            val domainList: List<Product> = initialCachedList.map { it.toDomain() }
            emit(Resource.Loading(domainList))
        }

        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            val apiResult = safeApiCall(ioDispatcher) { productApiHelper.getAllProducts() }

            when (apiResult) {
                is Resource.Success -> {
                    val networkList = apiResult.data!!
                    val dbList = networkList.map { it.toDBEntity() }
                    productDao.insertAll(dbList)
                }
                is Resource.Error -> {
                    if (!hasCache) {
                        emit(Resource.Error(apiResult.message!!, null))
                        return@flow
                    } else {
                        val domainList: List<Product> = initialCachedList.map { it.toDomain() }
                        emit(Resource.Error("Offline mode: Could not refresh data. ${apiResult.message}", domainList))
                    }
                }
                else -> Unit
            }
        } else {
            if (!hasCache) {
                emit(Resource.Error("You are offline and do not have any data.", null))
                return@flow
            } else {
                val domainList: List<Product> = initialCachedList.map { it.toDomain() }
                emit(Resource.Error("You are offline. Showing cached data.", domainList))
            }
        }


        productDao.getAllProducts().collect { finalCachedList ->
            if (finalCachedList.isNotEmpty()) {
                val domainList: List<Product> = finalCachedList.map { it.toDomain() }
                emit(Resource.Success(domainList))
            }
        }

    }.flowOn(ioDispatcher)


    override fun getProductDetail(productId: Int): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())

        val initialCachedProduct = productDao.getProductById(productId).first()
        val hasCache = initialCachedProduct != null

        if (hasCache) {
            val domainProduct: Product = initialCachedProduct.toDomain()
            emit(Resource.Loading(domainProduct))
        }

        val isOnline = networkMonitor.isOnline.first()

        if (isOnline) {
            val apiResult = safeApiCall(ioDispatcher) { productApiHelper.getProductById(productId) }

            when (apiResult) {
                is Resource.Success -> {
                    val networkProduct = apiResult.data!!
                    val dbProduct = networkProduct.toDBEntity() // New mapping
                    productDao.insert(dbProduct)
                }
                is Resource.Error -> {
                    if (!hasCache) {
                        emit(Resource.Error(apiResult.message!!, null))
                        return@flow
                    } else {
                        val domainProduct: Product = initialCachedProduct.toDomain()
                        emit(Resource.Error("Offline mode: Could not refresh data. ${apiResult.message}", domainProduct))
                    }
                }
                else -> Unit
            }
        } else {
            if (!hasCache) {
                emit(Resource.Error("You are offline and no data is cached.", null))
                return@flow
            } else {
                val domainProduct: Product = initialCachedProduct.toDomain()
                emit(Resource.Error("You are offline. Showing cached data.", domainProduct))
            }
        }

        productDao.getProductById(productId).collect { finalCachedProduct ->
            if (finalCachedProduct != null) {
                val domainProduct: Product = finalCachedProduct.toDomain()
                emit(Resource.Success(domainProduct))
            }
        }
    }.flowOn(ioDispatcher)
}