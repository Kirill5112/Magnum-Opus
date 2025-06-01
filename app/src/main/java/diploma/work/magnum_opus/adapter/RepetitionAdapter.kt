package diploma.work.magnum_opus.adapter

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
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
        const val TYPE_CHART = 2
        const val TYPE_TABLE_TITLE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_TEXT
            1 -> TYPE_TABLE_TITLE
            itemCount - 1 -> TYPE_CHART
            else -> TYPE_ITEM
        }
    }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.mv_item_text_MTV)
    }

    inner class TableTitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.mv_item_table_title_container)
    }

    inner class ChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chart: LineChart = itemView.findViewById(R.id.lineChart)
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

            TYPE_CHART -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.mv_item_chart, parent, false)
                ChartViewHolder(view)
            }

            TYPE_TABLE_TITLE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.mv_item_table_title, parent, false)
                TableTitleViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_TEXT -> {
                val textViewHolder = holder as TextViewHolder
                textViewHolder.tv.apply {
                    if (content.isNotBlank())
                        text = content
                    else {
                        setTextColor(ContextCompat.getColor(context, R.color.secondText))
                        text = "Содержимое пусто"
                    }
                    setBackgroundColor(Color.WHITE)
                }

            }

            TYPE_ITEM -> {
                val itemViewHolder = holder as ItemViewHolder
                itemViewHolder.apply {
                    val item = items[position - 2]
                    number.text = String.format(Locale.getDefault(), "%d", (position - 1))
                    valuation.text =
                        String.format(Locale.getDefault(), "%d", item.valuation)
                    date.text = convertMillisToDate(item.timestamp)
                    time.text = convertMillisToTime(item.timestamp)
                }
            }

            TYPE_CHART -> {
                val chartViewHolder = holder as ChartViewHolder
                chartViewHolder.apply {
                    if (items.size < 3)
                        chart.visibility = View.GONE
                    else {
                        val entries = items.mapIndexed { _, data ->
                            Entry(data.timestamp.toFloat(), data.valuation.toFloat())
                        }.toTypedArray()
                        val dataSet = LineDataSet(entries.toMutableList(), "Степень запоминания")
                        val desc = Description()
                        desc.text = "График степени запоминания материала"
                        dataSet.color = Color.BLUE
                        chartViewHolder.chart.apply {
                            xAxis.valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    val date = Date(value.toLong())
                                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        android.icu.text.SimpleDateFormat(
                                            "dd.MM",
                                            Locale.getDefault()
                                        )
                                            .format(date)
                                    } else {
                                        "old"
                                    }
                                }
                            }
                            data = LineData(dataSet)
                            description = desc
                        }
                    }
                }
            }

            TYPE_TABLE_TITLE -> {
                val table = holder as TableTitleViewHolder
                if (items.isEmpty())
                    table.container.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size + 3
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