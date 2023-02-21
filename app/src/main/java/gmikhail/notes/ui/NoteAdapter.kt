package gmikhail.notes.ui


import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import gmikhail.notes.data.db.Note
import gmikhail.notes.databinding.NoteRowItemBinding

class NoteAdapter(
    private val dataSet: Array<Note>,
    private val clickListener: AdapterItemClickListener
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    class ViewHolder(val binding: NoteRowItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NoteRowItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = dataSet[position].title
        holder.binding.textTitle.text = title
        holder.binding.textTitle.visibility = if(title.isNotBlank()) View.VISIBLE else View.GONE
        val body = dataSet[position].text
        holder.binding.textBody.text = body
        holder.binding.textBody.visibility = if(body.isNotBlank()) View.VISIBLE else View.GONE
        val timestamp = dataSet[position].lastModified
        val isToday = DateUtils.isToday(timestamp)
        holder.binding.textModified.text =
            if(isToday) DateUtils.getRelativeTimeSpanString(timestamp)
            else DateUtils.formatDateTime(holder.itemView.context, timestamp,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR)
        holder.itemView.setOnClickListener {
            clickListener.onClick(position)
        }
    }

    override fun getItemCount() = dataSet.count()

}

class AdapterItemClickListener(val clickListener: (position: Int) -> Unit) {
    fun onClick(position: Int) = clickListener(position)
}