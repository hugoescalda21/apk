package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.R
import com.congregation.reports.databinding.ActivityMainBinding
import com.congregation.reports.viewmodel.PublisherViewModel
import com.congregation.reports.viewmodel.ReportViewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var reportViewModel: ReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)

        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        reportViewModel = ViewModelProvider(this)[ReportViewModel::class.java]

        setupUI()
        observeData()
    }

    private fun setupUI() {
        binding.cardPublishers.setOnClickListener {
            animateCardClick(it)
            startActivityWithAnimation(PublisherListActivity::class.java)
        }

        binding.cardReports.setOnClickListener {
            animateCardClick(it)
            startActivityWithAnimation(ReportListActivity::class.java)
        }

        binding.cardMeetings.setOnClickListener {
            animateCardClick(it)
            startActivityWithAnimation(MeetingListActivity::class.java)
        }

        binding.cardBackup.setOnClickListener {
            animateCardClick(it)
            startActivityWithAnimation(BackupActivity::class.java)
        }

        binding.cardStatus.setOnClickListener {
            animateCardClick(it)
            startActivityWithAnimation(ReportStatusActivity::class.java)
        }

        binding.cardStats.setOnClickListener {
            animateCardClick(it)
            startActivityWithAnimation(StatisticsActivity::class.java)
        }
    }

    private fun animateCardClick(view: View) {
        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        view.startAnimation(scaleUp)
    }

    private fun startActivityWithAnimation(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        startActivity(intent, options.toBundle())
    }

    private fun observeData() {
        publisherViewModel.activePublishersCount.observe(this) { count ->
            binding.textPublishersCount.text = count?.toString() ?: "0"
        }

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)

        reportViewModel.getReportsCountForMonth(month, year).observe(this) { count ->
            binding.textReportsCount.text = count?.toString() ?: "0"
        }

        reportViewModel.getTotalHoursForMonth(month, year).observe(this) { hours ->
            binding.textTotalHours.text = hours?.toString() ?: "0"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_security -> {
                startActivity(Intent(this, SecuritySettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
