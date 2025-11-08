package com.nig.productexplorer.core.di

import com.nig.productexplorer.core.api.ProductApiHelper
import com.nig.productexplorer.core.api.ProductApiHelperImpl
import com.nig.productexplorer.data.repository.ProductRepository
import com.nig.productexplorer.data.repository.ProductRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    abstract fun bindProductRepository(
        repositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    abstract fun bindProductRemoteDataSource(
        remoteDataSourceImpl: ProductApiHelperImpl
    ): ProductApiHelper
}