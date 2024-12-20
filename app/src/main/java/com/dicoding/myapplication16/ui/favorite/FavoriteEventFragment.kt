package com.dicoding.myapplication16.ui.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.myapplication16.data.database.FavoriteEvent
import com.dicoding.myapplication16.data.database.retrofit.UiState
import com.dicoding.myapplication16.databinding.FragmentFavoriteEventBinding
import com.dicoding.myapplication16.ui.ViewModelFactory
import com.dicoding.myapplication16.ui.detail.DetailActivity


import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FavoriteEventFragment : Fragment() {
    private var _binding: FragmentFavoriteEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteEventViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }
    private lateinit var adapter: FavoriteEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = FavoriteEventAdapter { favoriteEvent ->
            navigateToDetailActivity(favoriteEvent.id)
        }
        binding.rvEvents.adapter = adapter
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success-> showFavorites(state.data)
                        is UiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.rvEvents.isVisible = false
        binding.tvNoData.isVisible = false
    }

    private fun showFavorites(favorites: List<FavoriteEvent>) {
        binding.progressBar.isVisible = false
        binding.rvEvents.isVisible = favorites.isNotEmpty()
        binding.tvNoData.isVisible = favorites.isEmpty()
        adapter.submitList(favorites)
    }

    private fun showError(message: String) {
        binding.progressBar.isVisible = false
        binding.rvEvents.isVisible = false
        binding.tvNoData.isVisible = true
        showErrorSnackbar(message)
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToDetailActivity(eventId: String) {
        val intent = Intent(requireContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EVENT_DETAIL, eventId)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}