package gmikhail.notes.ui

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import gmikhail.notes.R
import gmikhail.notes.databinding.NoteRowItemBinding
import gmikhail.notes.viewmodel.NoteState

class NoteAdapter(
    private val clickListener: AdapterItemClickListener,
    private val longClickListener: AdapterItemLongClickListener,
) : ListAdapter<NoteState, NoteAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = NoteRowItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            clickListener.onClick(holder.layoutPosition)
        }
        holder.itemView.setOnLongClickListener {
            longClickListener.onLongClick(holder.layoutPosition)
            true
        }
    }

    class ItemViewHolder(private val binding: NoteRowItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteState) = with(itemView) {
            val title = item.note.title
            binding.textTitle.text = title
            binding.textTitle.visibility = if(title.isNotBlank()) View.VISIBLE else View.GONE
            val body = item.note.text
            binding.textBody.text = body
            binding.textBody.visibility = if(body.isNotBlank()) View.VISIBLE else View.GONE
            val timestamp = item.note.lastModified
            val isToday = DateUtils.isToday(timestamp)
            binding.textModified.text =
                if(isToday) DateUtils.getRelativeTimeSpanString(timestamp)
                else DateUtils.formatDateTime(itemView.context, timestamp,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR)
            itemView.setBackgroundColor(
                if(item.isSelected) ContextCompat.getColor(context, R.color.selection_highlight)
                else Color.TRANSPARENT
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteState>() {
        override fun areItemsTheSame(oldItem: NoteState, newItem: NoteState): Boolean {
            return oldItem.note.uid == newItem.note.uid
        }

        override fun areContentsTheSame(oldItem: NoteState, newItem: NoteState): Boolean {
            return oldItem == newItem
        }
    }

    class AdapterItemClickListener(val clickListener: (position: Int) -> Unit) {
        fun onClick(position: Int) = clickListener(position)
    }

    class AdapterItemLongClickListener(val longClickListener: (position: Int) -> Unit) {
        fun onLongClick(position: Int) = longClickListener(position)
    }
}