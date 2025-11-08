package com.nig.productexplorer.presentation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.nig.productexplorer.core.util.Resource
import com.nig.productexplorer.databinding.FragmentProductDetailsBinding
import com.nig.productexplorer.domain.model.Product
import com.nig.productexplorer.presentation.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {
    private lateinit var binding: FragmentProductDetailsBinding
    private val viewModel: ProductViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productDetailState.filterNotNull().collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            if (resource.data == null) {
                                binding.detailLoadingIndicator.visibility = View.VISIBLE
                            } else {
                                binding.detailLoadingIndicator.visibility = View.GONE
                            }
                            resource.data?.let { updateUi(it) }
                        }
                        is Resource.Success -> {
                            binding.detailLoadingIndicator.visibility = View.GONE
                            resource.data?.let { updateUi(it) }
                        }
                        is Resource.Error -> {
                            binding.detailLoadingIndicator.visibility = View.GONE
                            Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                            resource.data?.let { updateUi(it) }
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun updateUi(product: Product) {
        binding.detailTitle.text = product.title
        binding.detailPrice.text = String.format("₦%.2f", product.price)
        binding.category.text = product.category
        binding.detailDescription.text = product.description
        binding.detailRating.rating = product.rating.toFloat()
        binding.detailRatingCount.text = String.format("(%d reviews)", product.ratingCount)

        binding.detailImage.load(product.imageUrl) {
            crossfade(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearDetailState()
    }
}