package com.congregation.reports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.data.Attendance
import com.congregation.reports.databinding.ItemAttendanceBinding
import com.congregation.reports.viewmodel.PublisherViewModel

class AttendanceAdapter(
    private val publisherViewModel: PublisherViewModel,
    private val onItemClick: (Attendance) -> Unit
) : ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(AttendanceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AttendanceViewHolder(private val binding: ItemAttendanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(attendance: Attendance) {
            publisherViewModel.getPublisherById(attendance.publisherId).observeForever { publisher ->
                binding.textPublisherName.text = publisher?.name ?: "Desconocido"
            }

            binding.checkBoxPresent.isChecked = attendance.wasPresent

            binding.checkBoxPresent.setOnClickListener {
                onItemClick(attendance)
            }

            binding.root.setOnClickListener {
                onItemClick(attendance)
            }
        }
    }

    class AttendanceDiffCallback : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance): Boolean {
            return oldItem == newItem
        }
    }
}
