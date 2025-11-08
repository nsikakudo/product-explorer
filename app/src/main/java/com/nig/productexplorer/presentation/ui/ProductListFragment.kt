package com.nig.productexplorer.presentation.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nig.productexplorer.R
import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.presentation.viewmodel.ProductViewModel
import com.nig.productexplorer.databinding.FragmentProductListBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {
    private lateinit var binding: FragmentProductListBinding
    private val viewModel: ProductViewModel by activityViewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupRefreshButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { productId ->
            viewModel.fetchProductDetails(productId)
            findNavController().navigate(R.id.action_productListFragment_to_productDetailFragment)
        }

        binding.productRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }
    }


    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            viewModel.fetchProducts()
            it.visibility = View.GONE
            binding.errorTextView.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productsState.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            if (resource.data == null) {
                                binding.loadingIndicator.visibility = View.VISIBLE
                            } else {
                                binding.loadingIndicator.visibility = View.GONE
                                binding.errorTextView.visibility = View.GONE
                                binding.refreshButton.visibility = View.GONE
                            }
                            resource.data?.let { productAdapter.submitList(it) }
                        }
                        is Resource.Success -> {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.errorTextView.visibility = View.GONE
                            binding.refreshButton.visibility = View.GONE
                            resource.data?.let { productAdapter.submitList(it) }
                        }
                        is Resource.Error -> {
                            binding.loadingIndicator.visibility = View.GONE
                            binding.errorTextView.text = resource.message
                            binding.errorTextView.visibility = View.VISIBLE
                            binding.refreshButton.visibility = View.VISIBLE
                            resource.data?.let { productAdapter.submitList(it) }
                        }
                    }
                }
            }
        }
    }
}