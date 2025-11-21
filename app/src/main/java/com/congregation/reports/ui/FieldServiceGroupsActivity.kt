package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.databinding.ActivityFieldServiceGroupsBinding
import com.congregation.reports.ui.adapters.FieldServiceGroupAdapter
import com.congregation.reports.viewmodel.FieldServiceGroupViewModel
import com.congregation.reports.viewmodel.PublisherViewModel

class FieldServiceGroupsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFieldServiceGroupsBinding
    private lateinit var groupViewModel: FieldServiceGroupViewModel
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var adapter: FieldServiceGroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFieldServiceGroupsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupViewModel = ViewModelProvider(this)[FieldServiceGroupViewModel::class.java]
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]

        setupRecyclerView()
        setupFab()
        observeData()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = FieldServiceGroupAdapter { group ->
            val intent = Intent(this, FieldServiceGroupDetailActivity::class.java)
            intent.putExtra("GROUP_ID", group.id)
            startActivity(intent)
        }

        binding.recyclerViewGroups.apply {
            layoutManager = LinearLayoutManager(this@FieldServiceGroupsActivity)
            adapter = this@FieldServiceGroupsActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddGroup.setOnClickListener {
            startActivity(Intent(this, FieldServiceGroupDetailActivity::class.java))
        }
    }

    private fun observeData() {
        groupViewModel.allActiveGroups.observe(this) { groups ->
            groups?.let {
                adapter.submitList(it)
            }
        }

        publisherViewModel.allPublishers.observe(this) { publishers ->
            publishers?.let {
                adapter.setPublishers(it)
            }
        }
    }
}
