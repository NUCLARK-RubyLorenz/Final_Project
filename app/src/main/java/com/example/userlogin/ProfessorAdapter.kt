package com.example.userlogin.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.userlogin.R
import com.example.userlogin.databinding.ItemProfessorBinding
import com.example.userlogin.model.Professor

class ProfessorAdapter(
    private val onFavoriteClick: (Professor) -> Unit
) : ListAdapter<Professor, ProfessorAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemProfessorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProfessorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val professor = getItem(position)

        holder.binding.apply {
            TVItemProfessorName.text = professor.username
            TVItemStatusText.text = if (professor.status) "Available" else "Busy"

            VStatusIndicator.setBackgroundResource(
                if (professor.status) R.drawable.status_on
                else R.drawable.status_off
            )

            IVFavorite.setImageResource(
                if (professor.isFavorite)
                    R.drawable.ic_heart_filled
                else
                    R.drawable.ic_heart_border
            )

            IVFavorite.setOnClickListener {
                onFavoriteClick(professor)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Professor>() {
        override fun areItemsTheSame(old: Professor, new: Professor): Boolean =
            old.uid == new.uid

        override fun areContentsTheSame(old: Professor, new: Professor): Boolean =
            old == new
    }
}
