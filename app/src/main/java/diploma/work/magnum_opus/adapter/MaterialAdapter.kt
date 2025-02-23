package diploma.work.magnum_opus.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.MaterialViewActivity
import diploma.work.magnum_opus.R
import diploma.work.magnum_opus.RepetitionActivity
import diploma.work.magnum_opus.item.ItemOfMaterialAdapter
import java.util.Calendar
import java.util.Locale


interface ListActionListener {
    fun isSelectionMode(isSelectionMode: Boolean)
    fun changeCounter(count: Int)
}

class MaterialAdapter(
    private val items: List<ItemOfMaterialAdapter>,
    private val listener: ListActionListener
) :
    RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    private val selectedItems: MutableSet<Int> = mutableSetOf()
    private var isSelectionMode = false
    private var timer: CountDownTimer? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.itemTextView)
        val timerTV: TextView = view.findViewById(R.id.itemTimerTV)
        val btnRep: ImageButton = view.findViewById(R.id.item_btn)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)
        return ViewHolder(view)
    }

    private fun selectionMode(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
            listener.changeCounter(selectedItems.size)
            if (selectedItems.size == 0) {
                isSelectionMode = false
                listener.isSelectionMode(false)
            }
        } else {
            selectedItems.add(position)
            listener.changeCounter(selectedItems.size)
            if (!isSelectionMode) {
                isSelectionMode = true
                listener.isSelectionMode(true)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = items[position]
        val trigger = item.timestamp - System.currentTimeMillis()
        toggleVisibilityBtn(trigger <= 0, viewHolder)
        viewHolder.apply {
            if (trigger > 0)
                startCountDownTimer(viewHolder, trigger)
            else if (item.isCompleted) {
                toggleVisibilityBtn(false, viewHolder)
                timerTV.text = "Завершено"
            }
            textView.text = item.title
            itemView.apply {
                isSelected = selectedItems.contains(position)
                setBackgroundColor(
                    if (selectedItems.contains(position)) Color.LTGRAY else Color.WHITE
                )
                setOnClickListener {
                    if (isSelectionMode) {
                        selectionMode(position)
                        notifyItemChanged(position)
                    } else {
                        val intent = Intent(context, MaterialViewActivity::class.java)
                        intent.putExtra("EXTRA_ID_LONG", item.id)
                        context.startActivity(intent)
                    }
                }
                setOnLongClickListener {
                    selectionMode(position)
                    notifyItemChanged(position)
                    true
                }
                btnRep.setOnClickListener {
                    if (isSelectionMode) {
                        selectionMode(position)
                        notifyItemChanged(position)
                    } else {
                        pressAnimation(it)
                        val intent = Intent(context, RepetitionActivity::class.java)
                        intent.putExtra("EXTRA_ID_LONG", item.id)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun getSelectedItems(): List<ItemOfMaterialAdapter> {
        return items.filterIndexed { index, _ -> selectedItems.contains(index) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyItemRangeChanged(0, itemCount)
    }

    private fun startCountDownTimer(viewHolder: ViewHolder, timeMillis: Long) {
        timer = object : CountDownTimer(timeMillis, 1000 * 60) {
            override fun onTick(timeM: Long) {
                viewHolder.timerTV.text = getTimerTime(timeM)
            }

            override fun onFinish() {
                toggleVisibilityBtn(true, viewHolder)
            }

        }.start()
    }

    private fun getTimerTime(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val currentCalendar = Calendar.getInstance().apply {
            this@apply.timeInMillis = now
        }
        val timerCalendar = Calendar.getInstance().apply {
            this@apply.timeInMillis = now + timeInMillis
        }
        return if (currentCalendar.get(Calendar.YEAR) == timerCalendar.get(Calendar.YEAR) &&
            currentCalendar.get(Calendar.DAY_OF_YEAR) == timerCalendar.get(Calendar.DAY_OF_YEAR)
        ) {
            val hours = (timeInMillis / ((1000 * 60 * 59))) % 24
            val minutes = ((timeInMillis / (1000 * 60)) + 1) % 60
            String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        } else {
            val day = timerCalendar.get(Calendar.DAY_OF_MONTH)
            val month = getMonthNameInGenitive(timerCalendar.get(Calendar.MONTH))
            "${day}-го $month"
        }
    }

    private fun getMonthNameInGenitive(month: Int): String {
        val monthsGenitive = arrayOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"
        )
        return monthsGenitive[month]
    }

    private fun toggleVisibilityBtn(btnIsVisible: Boolean, viewHolder: ViewHolder) {
        viewHolder.apply {
            if (btnIsVisible) {
                btnRep.visibility = View.VISIBLE
                timerTV.visibility = View.GONE
            } else {
                btnRep.visibility = View.GONE
                timerTV.visibility = View.VISIBLE
            }
        }
    }

}
