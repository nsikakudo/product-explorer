package com.nig.productexplorer.presentation.viewmodel

import app.cash.turbine.test
import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.data.repository.ProductRepository
import com.nig.productexplorer.domain.model.Product
import com.nig.productexplorer.domain.usecase.ProductCallUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val productRepository: ProductRepository = mock()
    private lateinit var productViewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val fakeProducts = listOf(
        Product(
            id = 1,
            title = "Test Product 1",
            price = 100.0,
            description = "Sample 1",
            category = "category1",
            imageUrl = "url1",
            rating = 4.5,
            ratingCount = 100
        ),
        Product(
            id = 2,
            title = "Test Product 2",
            price = 200.0,
            description = "Sample 2",
            category = "category2",
            imageUrl = "url2",
            rating = 3.9,
            ratingCount = 50
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        whenever(productRepository.getProducts()).thenReturn(
            flowOf(Resource.Success(emptyList()))
        )

        val productCallUseCases = ProductCallUseCases(productRepository)
        productViewModel = ProductViewModel(productCallUseCases)

        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchProducts emits Loading and then Success`() = runTest {
        whenever(productRepository.getProducts()).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(fakeProducts))
            }
        )

        productViewModel.productsState.test {
            val initial = awaitItem()
            assertTrue(initial is Resource.Success && initial.data!!.isEmpty(), "Initial state from init block should be empty Success.")

            productViewModel.fetchProducts()

            val loading = awaitItem()
            assertTrue(loading is Resource.Loading, "Next state should be Resource.Loading.")

            val success = awaitItem()
            assertTrue(success is Resource.Success && success.data == fakeProducts, "Final state should be Resource.Success.")

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchProducts emits Error when repository fails`() = runTest {
        val errorMessage = "Network Error"
        whenever(productRepository.getProducts()).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Error(errorMessage))
            }
        )

        productViewModel.productsState.test {
            val initial = awaitItem()
            assertTrue(initial is Resource.Success && initial.data!!.isEmpty(), "Initial state from init block should be empty Success.")

            productViewModel.fetchProducts()

            val loading = awaitItem()
            assertTrue(loading is Resource.Loading, "Next state should be Resource.Loading.")

            val error = awaitItem()
            assertTrue(error is Resource.Error && error.message == errorMessage, "Final state should be Resource.Error.")

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `fetchProductDetails emits Loading and Success`() = runTest {
        val productId = 1
        val product = fakeProducts.first()

        whenever(productRepository.getProductDetail(productId)).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Loading(product))
                emit(Resource.Success(product))
            }
        )

        productViewModel.productDetailState.test {
            val initial = awaitItem()
            assertTrue(initial == null, "Initial state must be null before fetch.")

            productViewModel.fetchProductDetails(productId)

            val loading = awaitItem()
            assertTrue(loading is Resource.Loading && loading.data == null, "Next state should be Resource.Loading without data.")

            val loadingWithCache = awaitItem()
            assertTrue(loadingWithCache is Resource.Loading && loadingWithCache.data == product, "Next state should be Resource.Loading with cached data.")

            val success = awaitItem()
            assertTrue(success is Resource.Success && success.data == product, "Final state should be Resource.Success.")

            cancelAndConsumeRemainingEvents()
        }
    }
}