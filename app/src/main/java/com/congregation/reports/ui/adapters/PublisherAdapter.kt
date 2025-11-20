package com.congregation.reports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.data.Publisher
import com.congregation.reports.databinding.ItemPublisherBinding

class PublisherAdapter(
    private val onItemClick: (Publisher) -> Unit
) : ListAdapter<Publisher, PublisherAdapter.PublisherViewHolder>(PublisherDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublisherViewHolder {
        val binding = ItemPublisherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PublisherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PublisherViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PublisherViewHolder(private val binding: ItemPublisherBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(publisher: Publisher) {
            binding.textPublisherName.text = publisher.name
            binding.textPublisherType.text = publisher.type.name.replace("_", " ")
            binding.textPublisherPhone.text = publisher.phoneNumber.ifEmpty { "Sin teléfono" }

            binding.root.setOnClickListener {
                onItemClick(publisher)
            }
        }
    }

    class PublisherDiffCallback : DiffUtil.ItemCallback<Publisher>() {
        override fun areItemsTheSame(oldItem: Publisher, newItem: Publisher): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Publisher, newItem: Publisher): Boolean {
            return oldItem == newItem
        }
    }
}
