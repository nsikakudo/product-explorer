package com.nig.productexplorer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.domain.model.Product
import com.nig.productexplorer.domain.usecase.ProductCallUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productCallUseCases: ProductCallUseCases
): ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    val productsState: StateFlow<Resource<List<Product>>> = _productsState.asStateFlow()

    private val _productDetailState = MutableStateFlow<Resource<Product>?>(null)
    val productDetailState: StateFlow<Resource<Product>?> = _productDetailState.asStateFlow()

    private var currentDetailId: Int? = null


    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            productCallUseCases.getAllProducts().collect { resource ->
                _productsState.value = resource
            }
        }
    }


    fun fetchProductDetails(productId: Int) {
        currentDetailId = productId

        viewModelScope.launch {
            productCallUseCases.getProductDetail(productId).collect { resource ->
                val emittedId = resource.data?.id
                if (emittedId == currentDetailId) {
                    _productDetailState.value = resource
                } else if (resource is Resource.Error && emittedId == null) {
                    _productDetailState.value = resource
                }
            }
        }
    }


    fun clearDetailState() {
        currentDetailId = null
        _productDetailState.value = null
    }
}