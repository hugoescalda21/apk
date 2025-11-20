package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.data.Publisher
import com.congregation.reports.databinding.ActivityPublisherListBinding
import com.congregation.reports.ui.adapters.PublisherAdapter
import com.congregation.reports.viewmodel.PublisherViewModel

class PublisherListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPublisherListBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var adapter: PublisherAdapter
    private var allPublishers: List<Publisher> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublisherListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Publicadores"

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        setupRecyclerView()
        setupFab()
        setupSearch()
        observePublishers()
    }

    private fun setupRecyclerView() {
        adapter = PublisherAdapter { publisher ->
            val intent = Intent(this, PublisherDetailActivity::class.java)
            intent.putExtra("PUBLISHER_ID", publisher.id)
            startActivity(intent)
        }

        binding.recyclerViewPublishers.apply {
            layoutManager = LinearLayoutManager(this@PublisherListActivity)
            adapter = this@PublisherListActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddPublisher.setOnClickListener {
            startActivity(Intent(this, PublisherDetailActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPublishers(newText ?: "")
                return true
            }
        })
    }

    private fun filterPublishers(query: String) {
        val filteredList = if (query.isEmpty()) {
            allPublishers
        } else {
            allPublishers.filter { publisher ->
                publisher.name.contains(query, ignoreCase = true) ||
                        publisher.phoneNumber.contains(query, ignoreCase = true) ||
                        publisher.email.contains(query, ignoreCase = true) ||
                        publisher.type.name.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredList)
    }

    private fun observePublishers() {
        publisherViewModel.allActivePublishers.observe(this) { publishers ->
            publishers?.let {
                allPublishers = it
                adapter.submitList(it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
