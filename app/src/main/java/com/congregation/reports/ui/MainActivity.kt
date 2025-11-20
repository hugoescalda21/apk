package com.congregation.reports.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import com.congregation.reports.R
import com.congregation.reports.data.Publisher
import com.congregation.reports.data.Report
import com.congregation.reports.databinding.ActivityMainBinding
import com.congregation.reports.databinding.BottomSheetQuickReportBinding
import com.congregation.reports.viewmodel.PublisherViewModel
import com.congregation.reports.viewmodel.ReportViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
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
            startActivityWithSharedElement(
                PublisherListActivity::class.java,
                it,
                "publishers_card_transition"
            )
        }

        binding.cardReports.setOnClickListener {
            startActivityWithSharedElement(
                ReportListActivity::class.java,
                it,
                "reports_card_transition"
            )
        }

        binding.cardMeetings.setOnClickListener {
            startActivityWithSharedElement(
                MeetingListActivity::class.java,
                it,
                "meetings_card_transition"
            )
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

        // FAB for quick report
        binding.fabQuickReport.setOnClickListener {
            showQuickReportBottomSheet()
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

    private fun startActivityWithSharedElement(activityClass: Class<*>, view: View, transitionName: String) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            view,
            transitionName
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

    private fun showQuickReportBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetQuickReportBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        var publishers: List<Publisher> = emptyList()
        var selectedPublisher: Publisher? = null

        // Observe publishers to populate dropdown
        publisherViewModel.allPublishers.observe(this) { publishersList ->
            publishers = publishersList
            val publisherNames = publishersList.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, publisherNames)
            bottomSheetBinding.autoCompletePublisher.setAdapter(adapter)
        }

        bottomSheetBinding.autoCompletePublisher.setOnItemClickListener { _, _, position, _ ->
            selectedPublisher = publishers[position]
        }

        bottomSheetBinding.buttonCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetBinding.buttonSave.setOnClickListener {
            if (selectedPublisher == null) {
                Toast.makeText(this, "Selecciona un publicador", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hours = bottomSheetBinding.editTextHours.text.toString().toIntOrNull() ?: 0
            val publications = bottomSheetBinding.editTextPublications.text.toString().toIntOrNull() ?: 0
            val videos = bottomSheetBinding.editTextVideos.text.toString().toIntOrNull() ?: 0
            val returnVisits = bottomSheetBinding.editTextReturnVisits.text.toString().toIntOrNull() ?: 0
            val bibleStudies = bottomSheetBinding.editTextBibleStudies.text.toString().toIntOrNull() ?: 0

            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            val report = Report(
                publisherId = selectedPublisher!!.id,
                month = month,
                year = year,
                hours = hours,
                publications = publications,
                videos = videos,
                returnVisits = returnVisits,
                bibleStudies = bibleStudies
            )

            reportViewModel.insert(report)
            Toast.makeText(this, "Informe guardado", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
