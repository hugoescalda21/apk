package com.congregation.reports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.data.Meeting
import com.congregation.reports.databinding.ItemMeetingBinding
import java.text.SimpleDateFormat
import java.util.*

class MeetingAdapter(
    private val onItemClick: (Meeting) -> Unit
) : ListAdapter<Meeting, MeetingAdapter.MeetingViewHolder>(MeetingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val binding = ItemMeetingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MeetingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MeetingViewHolder(private val binding: ItemMeetingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meeting: Meeting) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.textMeetingDate.text = sdf.format(Date(meeting.date))
            binding.textMeetingType.text = meeting.type.name.replace("_", " ")
            binding.textMeetingAttendance.text = "Asistencia: ${meeting.totalAttendance}"

            binding.root.setOnClickListener {
                onItemClick(meeting)
            }
        }
    }

    class MeetingDiffCallback : DiffUtil.ItemCallback<Meeting>() {
        override fun areItemsTheSame(oldItem: Meeting, newItem: Meeting): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Meeting, newItem: Meeting): Boolean {
            return oldItem == newItem
        }
    }
}
