package com.congregation.reports.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.databinding.ItemReportStatusBinding

class ReportStatusAdapter : ListAdapter<ReportStatusAdapter.PublisherStatus, ReportStatusAdapter.StatusViewHolder>(StatusDiffCallback()) {

    data class PublisherStatus(
        val publisherName: String,
        val hasSubmitted: Boolean,
        val publisherType: String
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemReportStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StatusViewHolder(private val binding: ItemReportStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(status: PublisherStatus) {
            binding.textPublisherName.text = status.publisherName
            binding.textPublisherType.text = status.publisherType

            if (status.hasSubmitted) {
                binding.textStatus.text = "✓ Entregado"
                binding.textStatus.setTextColor(Color.parseColor("#4CAF50"))
                binding.cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
            } else {
                binding.textStatus.text = "✗ Pendiente"
                binding.textStatus.setTextColor(Color.parseColor("#F44336"))
                binding.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
            }
        }
    }

    class StatusDiffCallback : DiffUtil.ItemCallback<PublisherStatus>() {
        override fun areItemsTheSame(oldItem: PublisherStatus, newItem: PublisherStatus): Boolean {
            return oldItem.publisherName == newItem.publisherName
        }

        override fun areContentsTheSame(oldItem: PublisherStatus, newItem: PublisherStatus): Boolean {
            return oldItem == newItem
        }
    }
}
