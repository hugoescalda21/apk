package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.databinding.ActivityPublisherListBinding
import com.congregation.reports.ui.adapters.PublisherAdapter
import com.congregation.reports.viewmodel.PublisherViewModel

class PublisherListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPublisherListBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var adapter: PublisherAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublisherListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Publicadores"

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        setupRecyclerView()
        setupFab()
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

    private fun observePublishers() {
        publisherViewModel.allActivePublishers.observe(this) { publishers ->
            publishers?.let {
                adapter.submitList(it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
