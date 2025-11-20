package com.congregation.reports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.data.Report
import com.congregation.reports.databinding.ItemReportBinding
import com.congregation.reports.viewmodel.PublisherViewModel

class ReportAdapter(
    private val publisherViewModel: PublisherViewModel,
    private val onItemClick: (Report) -> Unit
) : ListAdapter<Report, ReportAdapter.ReportViewHolder>(ReportDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            publisherViewModel.getPublisherById(report.publisherId).observeForever { publisher ->
                binding.textReportPublisher.text = publisher?.name ?: "Desconocido"
            }

            binding.textReportHours.text = "Horas: ${report.hours}"
            binding.textReportPublications.text = "Publicaciones: ${report.publications}"
            binding.textReportStudies.text = "Estudios: ${report.bibleStudies}"

            binding.root.setOnClickListener {
                onItemClick(report)
            }
        }
    }

    class ReportDiffCallback : DiffUtil.ItemCallback<Report>() {
        override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
            return oldItem == newItem
        }
    }
}
