package com.dicoding.myapplication16.ui.detail


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dicoding.myapplication16.R
import com.dicoding.myapplication16.data.database.response.Event
import com.dicoding.myapplication16.data.database.retrofit.UiState
import com.dicoding.myapplication16.databinding.ActivityDetailBinding

import com.dicoding.myapplication16.ui.ViewModelFactory


import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    companion object {
        const val EVENT_DETAIL = "event_detail"
    }

    private lateinit var binding: ActivityDetailBinding

    private val viewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val eventId = intent.getStringExtra(EVENT_DETAIL) ?: return finish()

        viewModel.fetchEventDetails(eventId)
        observeViewModel()
        setupFavoriteButton()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.cardView.isVisible = false
                    }

                    is UiState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.cardView.isVisible = true
                        state.data?.let { displayEventDetails(it) }
                            ?: showSnackbar("Event details not available")
                    }

                    is UiState.Error -> {
                        binding.progressBar.isVisible = false
                        binding.cardView.isVisible = false
                        showSnackbar(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isFavorite.collect { isFavorite ->
                updateFavoriteButtonState(isFavorite)
            }
        }
    }

    private fun setupFavoriteButton() {
        binding.fabAdd.setOnClickListener {
            viewModel.toggleFavorite()
        }
    }

    private fun updateFavoriteButtonState(isFavorite: Boolean) {
        binding.fabAdd.setImageResource(
            if (isFavorite) R.drawable.baseline_favorite_24
            else R.drawable.baseline_favorite_border_24
        )
    }


    private fun displayEventDetails(event: Event) {
        with(binding) {
            Glide.with(this@DetailActivity)
                .load(event.mediaCover)
                .into(ivEventImage)

            tvEventName.text = event.name
            tvEventSummary.text = event.summary
            tvEventCategory.text = getString(R.string.category_format, event.category)
            tvEventOwner.text = getString(R.string.organizer_format, event.ownerName)
            tvEventCity.text = getString(R.string.location_format, event.cityName)
            tvEventDate.text = getString(R.string.date_format,
                event.beginTime?.let { formatDate(it) })
            tvEventTime.text = getString(R.string.time_format,
                event.beginTime?.let { formatTime(it) }, event.endTime?.let { formatTime(it) })
            //tvEventQuota.text = getString(R.string.quota_format, event.quota)
            tvEventRegistrants.text = getString(R.string.registrants_format, event.registrants)

            tvEventQuota.text = "Sisa Kuota: ${(event.quota ?: 0) - (event.registrants ?: 0)}"

            tvEventDescription.text = HtmlCompat.fromHtml(
                event.description ?: "",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            btnRegister.setOnClickListener {
                event.link?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                }
            }
        }
    }


    private fun formatDate(dateTimeString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateTimeString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateTimeString
        }
    }

    private fun formatTime(dateTimeString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return try {
            val date = inputFormat.parse(dateTimeString)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateTimeString
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.main, message, Snackbar.LENGTH_LONG).show()
    }
}