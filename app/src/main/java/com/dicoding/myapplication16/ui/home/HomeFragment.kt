package com.dicoding.myapplication16.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.myapplication16.data.database.response.ListEventsItem
import com.dicoding.myapplication16.data.database.retrofit.UiState
import com.dicoding.myapplication16.databinding.FragmentHomeBinding
import com.dicoding.myapplication16.ui.EventAdapter
import com.dicoding.myapplication16.ui.EventViewModel
import com.dicoding.myapplication16.ui.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by viewModels { ViewModelFactory.getInstance(requireContext()) }
    private lateinit var upcomingAdapter: EventAdapter
    private lateinit var finishedAdapter: EventAdapter
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var snackbar: Snackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupNetworkCallback()
        observeViewModel()
        checkNetworkAndFetchEvents()
    }

    private fun setupRecyclerViews() {
        upcomingAdapter = EventAdapter()
        finishedAdapter = EventAdapter()

        binding.rvUpcomingEvents.apply {
            adapter = upcomingAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.rvFinishedEvents.apply {
            adapter = finishedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupNetworkCallback() {
        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.setNetworkState(true)
                    viewModel.fetchEvents(1) // Fetch upcoming events
                    viewModel.fetchEvents(0) // Fetch finished events
                }
            }

            override fun onLost(network: Network) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.setNetworkState(false)
                }
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkNetworkAndFetchEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            val isNetworkAvailable = isNetworkAvailable()
            viewModel.setNetworkState(isNetworkAvailable)
            if (isNetworkAvailable) {
                viewModel.fetchEvents(1) // Fetch upcoming events data
                viewModel.fetchEvents(0) // Fetch finished events data
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> {
                        // Check the type of the event data
                        if (state.type == 1) { // Assuming `type` is an integer in the `UiState.Success` class
                            showUpcomingEvents(state.data)
                        } else {
                            showFinishedEvents(state.data)
                        }
                    }
                    is UiState.Error -> showError(state.message)
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.networkState.collect { isConnected ->
                if (!isConnected) {
                    showNetworkErrorSnackbar()
                } else {
                    snackbar?.dismiss()
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.rvUpcomingEvents.isVisible = false
        binding.rvFinishedEvents.isVisible = false
        binding.tvNoUpcomingData.isVisible = false
        binding.tvNoFinishedData.isVisible = false
    }

    private fun showUpcomingEvents(events: List<ListEventsItem>) {
        binding.progressBar.isVisible = false
        binding.rvUpcomingEvents.isVisible = true
        binding.tvNoUpcomingData.isVisible = events.isEmpty()
        upcomingAdapter.submitList(events)
    }

    private fun showFinishedEvents(events: List<ListEventsItem>) {
        binding.progressBar.isVisible = false
        binding.rvFinishedEvents.isVisible = true
        binding.tvNoFinishedData.isVisible = events.isEmpty()
        finishedAdapter.submitList(events)
    }

    private fun showError(message: String) {
        binding.progressBar.isVisible = false
        binding.rvUpcomingEvents.isVisible = false
        binding.rvFinishedEvents.isVisible = false
        binding.tvNoUpcomingData.isVisible = true
        binding.tvNoFinishedData.isVisible = true
        showErrorSnackbar(message)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showNetworkErrorSnackbar() {
        snackbar?.dismiss()
        val rootView = activity?.findViewById<View>(android.R.id.content)
        rootView?.let {
            snackbar = Snackbar.make(
                it,
                "Network not detected. Please check your internet connection.",
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setAction("Retry") {
                    checkNetworkAndFetchEvents()
                }
                show()
            }
        }
    }

    private fun showErrorSnackbar(message: String) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply { show() }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun isNetworkAvailable(): Boolean = withContext(Dispatchers.IO) {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        _binding = null
    }
}
