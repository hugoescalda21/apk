package com.congregation.reports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.data.FieldServiceGroup
import com.congregation.reports.data.Publisher
import com.congregation.reports.databinding.ItemFieldServiceGroupBinding

class FieldServiceGroupAdapter(
    private val onClick: (FieldServiceGroup) -> Unit
) : ListAdapter<FieldServiceGroup, FieldServiceGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    private var allPublishers: List<Publisher> = emptyList()

    fun setPublishers(publishers: List<Publisher>) {
        allPublishers = publishers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemFieldServiceGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GroupViewHolder(
        private val binding: ItemFieldServiceGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: FieldServiceGroup) {
            binding.textGroupName.text = group.name

            // Find overseer name
            val overseer = allPublishers.find { it.id == group.overseerPublisherId }
            binding.textOverseer.text = if (overseer != null) {
                "Superintendente: ${overseer.name}"
            } else {
                "Superintendente: No asignado"
            }

            // Find assistant name
            val assistant = allPublishers.find { it.id == group.assistantPublisherId }
            binding.textAssistant.text = if (assistant != null) {
                "Ayudante: ${assistant.name}"
            } else {
                "Ayudante: No asignado"
            }

            // Count members
            val memberCount = allPublishers.count { it.groupId == group.id }
            binding.textMemberCount.text = "👥 $memberCount publicador${if (memberCount != 1) "es" else ""}"

            binding.root.setOnClickListener { onClick(group) }
        }
    }

    private class GroupDiffCallback : DiffUtil.ItemCallback<FieldServiceGroup>() {
        override fun areItemsTheSame(oldItem: FieldServiceGroup, newItem: FieldServiceGroup): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FieldServiceGroup, newItem: FieldServiceGroup): Boolean {
            return oldItem == newItem
        }
    }
}
