package com.congregation.reports.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.congregation.reports.databinding.ItemBackupBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupAdapter(
    private val onRestore: (File) -> Unit,
    private val onShare: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : ListAdapter<File, BackupAdapter.BackupViewHolder>(BackupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val binding = ItemBackupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BackupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BackupViewHolder(private val binding: ItemBackupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.textBackupName.text = file.name
            binding.textBackupDate.text = sdf.format(Date(file.lastModified()))
            binding.textBackupSize.text = formatFileSize(file.length())

            binding.buttonRestore.setOnClickListener {
                onRestore(file)
            }

            binding.buttonShare.setOnClickListener {
                onShare(file)
            }

            binding.buttonDelete.setOnClickListener {
                onDelete(file)
            }
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }
    }

    class BackupDiffCallback : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.lastModified() == newItem.lastModified()
        }
    }
}
