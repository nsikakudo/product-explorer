package com.nig.productexplorer.presentation.viewmodel

import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.data.repository.ProductRepository
import com.nig.productexplorer.domain.model.Product
import com.nig.productexplorer.domain.usecase.ProductCallUseCases
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.mockito.kotlin.*
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

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

        // --- CORRECTION: STUB A DEFAULT SUCCESS FOR THE init BLOCK ---
        // The init block calls fetchProducts(), which calls productRepository.getProducts().
        // It must return a non-null Flow before the ViewModel is instantiated.
        whenever(productRepository.getProducts()).thenReturn(
            flowOf(Resource.Success(emptyList())) // Stub with an initial non-crashing value
        )

        // Wrap repository in ProductCallUseCases
        val productCallUseCases = ProductCallUseCases(productRepository)

        // Pass ProductCallUseCases to ViewModel. The init block executes here.
        productViewModel = ProductViewModel(productCallUseCases)

        // Advance the dispatcher to allow the init block's launch block to complete.
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchProducts emits Loading and then Success`() = runTest {
        // Given
        // RE-STUB the mock for this specific test case.
        // The previous stub was just to prevent crash in init.
        whenever(productRepository.getProducts()).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(fakeProducts))
            }
        )

        // When - Start collecting states *before* the fetch is triggered to capture all emissions
        val states = mutableListOf<Resource<List<Product>>>()
        val job = launch {
            // Skip the initial state from init() and only collect new states triggered by fetchProducts()
            // Or, collect all states and check for the desired sequence.
            productViewModel.productsState.drop(1).toList(states)
        }

        // Trigger the fetch again
        productViewModel.fetchProducts()

        // Advance to collect the emissions from the re-fetched flow
        advanceUntilIdle()
        job.cancel()

        // Then
        assertTrue(states.isNotEmpty(), "The states list should not be empty.")
        assertTrue(states.first() is Resource.Loading, "The first emitted state should be Resource.Loading.")
        assertTrue(states.last() is Resource.Success && states.last().data == fakeProducts, "The last emitted state should be Resource.Success with fake products.")
    }

    @Test
    fun `fetchProducts emits Error when repository fails`() = runTest {
        // Given
        whenever(productRepository.getProducts()).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Error("Network Error"))
            }
        )

        // When
        val results = mutableListOf<Resource<List<Product>>>()
        val job = launch {
            // Skip the initial state from init() and only collect new states triggered by fetchProducts()
            productViewModel.productsState.drop(1).toList(results)
        }

        productViewModel.fetchProducts()
        advanceUntilIdle() // Allow coroutine to run and flow to emit
        job.cancel()

        // Then
        assertTrue(results.isNotEmpty(), "The results list should not be empty.")
        assertTrue(results.any { it is Resource.Error && it.message == "Network Error" }, "Should contain Resource.Error with 'Network Error' message.")
    }

    @Test
    fun `fetchProductDetails emits Loading and Success`() = runTest {
        // Given
        val productId = 1
        val product = fakeProducts.first()

        // Stub the specific detail call
        whenever(productRepository.getProductDetail(productId)).thenReturn(
            flow {
                emit(Resource.Loading())
                emit(Resource.Success(product))
            }
        )

        // When - Start collecting states *before* the fetch is triggered to capture all emissions
        val results = mutableListOf<Resource<Product>>()
        val job = launch {
            // Collect the StateFlow. filterNotNull() is important because it starts as null.
            productViewModel.productDetailState.filterNotNull().toList(results)
        }

        productViewModel.fetchProductDetails(productId) // Triggers the detail fetch

        advanceUntilIdle() // Allow coroutine to run and flow to emit
        job.cancel()

        // Then
        assertTrue(results.isNotEmpty(), "The results list should not be empty.")
        assertTrue(results.first() is Resource.Loading, "The first emitted state should be Resource.Loading.")
        assertTrue(results.last() is Resource.Success && results.last().data == product, "The last emitted state should be Resource.Success with the product.")
    }
}



//@OptIn(ExperimentalCoroutinesApi::class)
//class ProductViewModelTest {
//
//    // This is the class-level mock that will be stubbed with 'whenever'
//    private val productRepository: ProductRepository = mock()
//    private lateinit var productViewModel: ProductViewModel
//    private val testDispatcher = StandardTestDispatcher()
//
//    private val fakeProducts = listOf(
//        Product(
//            id = 1,
//            title = "Test Product 1",
//            price = 100.0,
//            description = "Sample 1",
//            category = "category1",
//            imageUrl = "url1",
//            rating = 4.5,
//            ratingCount = 100
//        ),
//        Product(
//            id = 2,
//            title = "Test Product 2",
//            price = 200.0,
//            description = "Sample 2",
//            category = "category2",
//            imageUrl = "url2",
//            rating = 3.9,
//            ratingCount = 50
//        )
//    )
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//
//        // --- CORRECTION APPLIED HERE ---
//        // 1. Removed the local 'val productRepository: ProductRepository = mock()' declaration.
//
//        // 2. Wrap the class-level productRepository in ProductCallUseCases
//        val productCallUseCases = ProductCallUseCases(productRepository)
//
//        // 3. Pass ProductCallUseCases to ViewModel
//        productViewModel = ProductViewModel(productCallUseCases)
//
//        // Advance the dispatcher to allow the init block (which calls fetchProducts) to execute its launch block
//        testDispatcher.scheduler.advanceUntilIdle()
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `fetchProducts emits Loading and then Success`() = runTest {
//        // Given
//        // Since the ViewModel's init block calls fetchProducts(), we stub the repository
//        // before the ViewModel is initialized in the setup block.
//        // The setup block now includes advanceUntilIdle(), which runs the init block.
//        // We will explicitly call fetchProducts() for clarity and to simulate a manual refresh.
//
//        whenever(productRepository.getProducts()).thenReturn(
//            flow {
//                emit(Resource.Loading()) // Loading from API call
//                emit(Resource.Success(fakeProducts))
//            }
//        )
//
//        // Reset the state to force a re-fetch, as the init block has already run
//        // We need to re-initialize the ViewModel to guarantee collecting from the start,
//        // or just call fetchProducts() and collect *after* the fetch is triggered.
//
//        // Re-initialize the ViewModel (optional, but ensures a clean start for the state flow collection)
//        val productCallUseCases = ProductCallUseCases(productRepository)
//        val freshViewModel = ProductViewModel(productCallUseCases)
//
//        // Since the init block already runs in the ViewModel's constructor,
//        // we advance past it. We need to collect the states from the beginning.
//
//        // When - Start collecting states
//        val states = mutableListOf<Resource<List<Product>>>()
//        val job = launch {
//            freshViewModel.productsState.toList(states)
//        }
//
//        // Advance to collect the initial Resource.Loading() from the StateFlow initialization
//        advanceUntilIdle()
//
//        // Then
//        job.cancel()
//
//        // The collected list should be:
//        // 1. Resource.Loading (from StateFlow initialization)
//        // 2. Resource.Loading (from repository flow)
//        // 3. Resource.Success(fakeProducts) (from repository flow)
//
//        assertTrue(states.isNotEmpty(), "The states list should not be empty.")
//        assertTrue(states.any { it is Resource.Success && it.data == fakeProducts }, "Should contain Resource.Success with fake products.")
//        assertTrue(states.first() is Resource.Loading, "The initial state should be Resource.Loading.")
//    }
//
//    @Test
//    fun `fetchProducts emits Error when repository fails`() = runTest {
//        whenever(productRepository.getProducts()).thenReturn(
//            flow {
//                emit(Resource.Loading())
//                emit(Resource.Error("Network Error"))
//            }
//        )
//
//        // Re-initialize the ViewModel to ensure the stubbing is picked up by the init block
//        val productCallUseCases = ProductCallUseCases(productRepository)
//        val freshViewModel = ProductViewModel(productCallUseCases)
//
//        // When
//        val results = mutableListOf<Resource<List<Product>>>()
//        val job = launch { freshViewModel.productsState.toList(results) }
//
//        advanceUntilIdle() // Allow init block and flow collection to complete
//        job.cancel()
//
//        // Then
//        assertTrue(results.isNotEmpty(), "The results list should not be empty.")
//        assertTrue(results.any { it is Resource.Error && it.message == "Network Error" }, "Should contain Resource.Error with 'Network Error' message.")
//    }
//
//    @Test
//    fun `fetchProductDetails emits Loading and Success`() = runTest {
//        val productId = 1
//        val product = fakeProducts.first()
//
//        whenever(productRepository.getProductDetail(productId)).thenReturn(
//            flow {
//                emit(Resource.Loading())
//                emit(Resource.Success(product))
//            }
//        )
//
//        // When
//        productViewModel.fetchProductDetails(productId) // Triggers the detail fetch
//
//        val results = mutableListOf<Resource<Product>>()
//        val job = launch {
//            // Collect the StateFlow. filterNotNull() is important because it starts as null.
//            productViewModel.productDetailState.filterNotNull().toList(results)
//        }
//
//        advanceUntilIdle() // Allow coroutine to run and flow to emit
//        job.cancel()
//
//        // Then
//        assertTrue(results.isNotEmpty(), "The results list should not be empty.")
//        assertTrue(results.first() is Resource.Loading, "The first emitted state should be Resource.Loading.")
//        assertTrue(results.last() is Resource.Success && results.last().data == product, "The last emitted state should be Resource.Success with the product.")
//    }
//}
