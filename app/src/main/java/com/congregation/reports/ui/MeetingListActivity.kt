package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.databinding.ActivityMeetingListBinding
import com.congregation.reports.ui.adapters.MeetingAdapter
import com.congregation.reports.viewmodel.MeetingViewModel

class MeetingListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMeetingListBinding
    private lateinit var meetingViewModel: MeetingViewModel
    private lateinit var adapter: MeetingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reuniones"

        meetingViewModel = ViewModelProvider(this)[MeetingViewModel::class.java]

        setupRecyclerView()
        setupFab()
        observeMeetings()
    }

    private fun setupRecyclerView() {
        adapter = MeetingAdapter { meeting ->
            val intent = Intent(this, MeetingDetailActivity::class.java)
            intent.putExtra("MEETING_ID", meeting.id)
            startActivity(intent)
        }

        binding.recyclerViewMeetings.apply {
            layoutManager = LinearLayoutManager(this@MeetingListActivity)
            adapter = this@MeetingListActivity.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddMeeting.setOnClickListener {
            startActivity(Intent(this, MeetingDetailActivity::class.java))
        }
    }

    private fun observeMeetings() {
        meetingViewModel.allMeetings.observe(this) { meetings ->
            meetings?.let {
                adapter.submitList(it)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
