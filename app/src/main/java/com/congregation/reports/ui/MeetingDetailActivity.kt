package com.congregation.reports.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.congregation.reports.data.Attendance
import com.congregation.reports.data.Meeting
import com.congregation.reports.data.MeetingType
import com.congregation.reports.databinding.ActivityMeetingDetailBinding
import com.congregation.reports.ui.adapters.AttendanceAdapter
import com.congregation.reports.viewmodel.AttendanceViewModel
import com.congregation.reports.viewmodel.MeetingViewModel
import com.congregation.reports.viewmodel.PublisherViewModel
import java.text.SimpleDateFormat
import java.util.*

class MeetingDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMeetingDetailBinding
    private lateinit var meetingViewModel: MeetingViewModel
    private lateinit var publisherViewModel: PublisherViewModel
    private lateinit var attendanceViewModel: AttendanceViewModel
    private lateinit var adapter: AttendanceAdapter
    private var meetingId: Long = 0
    private var currentMeeting: Meeting? = null
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        meetingViewModel = ViewModelProvider(this)[MeetingViewModel::class.java]
        publisherViewModel = ViewModelProvider(this)[PublisherViewModel::class.java]
        attendanceViewModel = ViewModelProvider(this)[AttendanceViewModel::class.java]

        meetingId = intent.getLongExtra("MEETING_ID", 0)

        setupMeetingTypeSpinner()
        setupDatePicker()
        setupButtons()
        setupRecyclerView()

        if (meetingId > 0) {
            supportActionBar?.title = "Editar Reunión"
            loadMeeting()
            loadAttendance()
        } else {
            supportActionBar?.title = "Nueva Reunión"
        }
    }

    private fun setupMeetingTypeSpinner() {
        val types = MeetingType.values().map { it.name.replace("_", " ") }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMeetingType.adapter = adapter
    }

    private fun setupDatePicker() {
        updateDateDisplay()
        binding.buttonSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.timeInMillis
                    updateDateDisplay()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateDisplay() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.textSelectedDate.text = sdf.format(Date(selectedDate))
    }

    private fun loadMeeting() {
        meetingViewModel.getMeetingById(meetingId).observe(this) { meeting ->
            meeting?.let {
                currentMeeting = it
                selectedDate = it.date
                updateDateDisplay()
                binding.spinnerMeetingType.setSelection(it.type.ordinal)
                binding.editTextTotalAttendance.setText(it.totalAttendance.toString())
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AttendanceAdapter(publisherViewModel) { attendance ->
            attendanceViewModel.update(attendance.copy(wasPresent = !attendance.wasPresent))
        }

        binding.recyclerViewAttendance.apply {
            layoutManager = LinearLayoutManager(this@MeetingDetailActivity)
            adapter = this@MeetingDetailActivity.adapter
        }
    }

    private fun loadAttendance() {
        if (meetingId > 0) {
            binding.attendanceSection.visibility = View.VISIBLE
            attendanceViewModel.getAttendanceByMeeting(meetingId).observe(this) { attendanceList ->
                attendanceList?.let {
                    adapter.submitList(it)
                }
            }

            binding.buttonMarkAll.setOnClickListener {
                publisherViewModel.allActivePublishers.observe(this) { publishers ->
                    publishers?.forEach { publisher ->
                        val attendance = Attendance(
                            publisherId = publisher.id,
                            meetingId = meetingId,
                            wasPresent = true
                        )
                        attendanceViewModel.insert(attendance)
                    }
                }
            }
        } else {
            binding.attendanceSection.visibility = View.GONE
        }
    }

    private fun setupButtons() {
        binding.buttonSaveMeeting.setOnClickListener {
            saveMeeting()
        }

        if (meetingId > 0) {
            binding.buttonDeleteMeeting.visibility = View.VISIBLE
            binding.buttonDeleteMeeting.setOnClickListener {
                deleteMeeting()
            }
        }
    }

    private fun saveMeeting() {
        val type = MeetingType.values()[binding.spinnerMeetingType.selectedItemPosition]
        val totalAttendance = binding.editTextTotalAttendance.text.toString().toIntOrNull() ?: 0

        val meeting = Meeting(
            id = meetingId,
            type = type,
            date = selectedDate,
            totalAttendance = totalAttendance
        )

        if (meetingId > 0) {
            meetingViewModel.update(meeting)
            Toast.makeText(this, "Reunión actualizada", Toast.LENGTH_SHORT).show()
        } else {
            meetingViewModel.insert(meeting)
            Toast.makeText(this, "Reunión creada", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun deleteMeeting() {
        currentMeeting?.let {
            meetingViewModel.delete(it)
            Toast.makeText(this, "Reunión eliminada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
