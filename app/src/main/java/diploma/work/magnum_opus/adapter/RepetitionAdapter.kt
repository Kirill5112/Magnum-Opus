package diploma.work.magnum_opus.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import diploma.work.magnum_opus.R
import diploma.work.magnum_opus.item.ItemOfRepetitionAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RepetitionAdapter(
    private val items: List<ItemOfRepetitionAdapter>,
    private val content: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_TEXT
        } else {
            TYPE_ITEM
        }
    }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.mv_item_text_MTV)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val number: TextView = itemView.findViewById(R.id.mv_item_list_number)
        val valuation: TextView = itemView.findViewById(R.id.mv_item_list_valuation)
        val date: TextView = itemView.findViewById(R.id.mv_item_list_date)
        val time: TextView = itemView.findViewById(R.id.mv_item_list_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.mv_item_text, parent, false)
                TextViewHolder(view)
            }

            TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.mv_item_list, parent, false)
                ItemViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_TEXT -> {
                val textViewHolder = holder as TextViewHolder
                textViewHolder.tv.apply {
                    text = content
                    setBackgroundColor(Color.WHITE)
                }

            }

            TYPE_ITEM -> {
                val itemViewHolder = holder as ItemViewHolder
                itemViewHolder.apply {
                    val item = items[position - 1]
                    number.text = String.format(Locale.getDefault(), "%d", position)
                    valuation.text =
                        String.format(Locale.getDefault(), "%d", item.valuation)
                    date.text = convertMillisToDate(item.timestamp)
                    time.text = convertMillisToTime(item.timestamp)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1
    }

    private fun convertMillisToDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = Date(millis)
        return sdf.format(date)
    }

    private fun convertMillisToTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = Date(millis)
        return sdf.format(date)
    }
}