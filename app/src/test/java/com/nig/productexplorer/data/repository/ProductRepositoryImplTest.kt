package com.nig.productexplorer.data.repository

import com.nig.productexplorer.core.api.ProductApiHelper
import com.nig.productexplorer.core.network.NetworkMonitor
import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.data.local.ProductDao
import com.nig.productexplorer.data.local.toDBEntity
import com.nig.productexplorer.data.remote.NetworkRating
import com.nig.productexplorer.data.remote.ProductRemoteEntity
import com.nig.productexplorer.domain.model.Product
import com.nig.productexplorer.domain.model.toDomain
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryImplTest {

    private lateinit var productRepository: ProductRepositoryImpl
    private val productApiHelper: ProductApiHelper = mock()
    private val productDao: ProductDao = mock()
    private val networkMonitor: NetworkMonitor = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        productRepository = ProductRepositoryImpl(
            productApiHelper = productApiHelper,
            productDao = productDao,
            networkMonitor = networkMonitor,
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getProducts returns success when api and db work correctly`() = runTest {
        val fakeProduct = ProductRemoteEntity(
            id = 1,
            title = "Test Product",
            price = 100.0,
            description = "Sample",
            category = "test",
            image = "url",
            rating = NetworkRating(4.5, 10)
        )
        val fakeEntity = fakeProduct.toDBEntity()
        val fakeDomain = fakeEntity.toDomain()

        whenever(productDao.getAllProducts()).thenReturn(flowOf(listOf(fakeEntity)))
        whenever(networkMonitor.isOnline).thenReturn(flowOf(true))
        whenever(productApiHelper.getAllProducts()).thenReturn(listOf(fakeProduct))

        val results = mutableListOf<Resource<List<Product>>>()
        val job = launch { productRepository.getProducts().toList(results) }
        advanceUntilIdle()
        job.cancel()

        assertTrue(results.any { it is Resource.Success && it.data!!.contains(fakeDomain) })
    }

    @Test
    fun `getProducts emits error when offline and no cache`() = runTest {
        whenever(productDao.getAllProducts()).thenReturn(flowOf(emptyList()))
        whenever(networkMonitor.isOnline).thenReturn(flowOf(false))

        val results = mutableListOf<Resource<List<Product>>>()
        val job = launch { productRepository.getProducts().toList(results) }
        advanceUntilIdle()
        job.cancel()

        val error = results.last()
        assertTrue(error is Resource.Error)
        assertEquals("You are offline and do not have any data.", error.message)
    }
}