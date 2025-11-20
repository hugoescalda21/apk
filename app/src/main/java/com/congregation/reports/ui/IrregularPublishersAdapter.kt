package com.congregation.reports.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.data.Publisher
import com.congregation.reports.databinding.ItemIrregularPublisherBinding

class IrregularPublishersAdapter : ListAdapter<Publisher, IrregularPublishersAdapter.ViewHolder>(PublisherDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIrregularPublisherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemIrregularPublisherBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(publisher: Publisher) {
            binding.textPublisherName.text = publisher.name
            binding.textPublisherType.text = when (publisher.type) {
                com.congregation.reports.data.PublisherType.PUBLICADOR -> "Publicador"
                com.congregation.reports.data.PublisherType.PRECURSOR_AUXILIAR -> "Precursor Auxiliar"
                com.congregation.reports.data.PublisherType.PRECURSOR_REGULAR -> "Precursor Regular"
                com.congregation.reports.data.PublisherType.PRECURSOR_ESPECIAL -> "Precursor Especial"
            }
        }
    }

    private class PublisherDiffCallback : DiffUtil.ItemCallback<Publisher>() {
        override fun areItemsTheSame(oldItem: Publisher, newItem: Publisher): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Publisher, newItem: Publisher): Boolean {
            return oldItem == newItem
        }
    }
}
