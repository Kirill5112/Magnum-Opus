package diploma.work.magnum_opus.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import androidx.recyclerview.widget.RecyclerView
import diploma.work.magnum_opus.IntervalsViewActivity
import diploma.work.magnum_opus.R
import diploma.work.magnum_opus.model.Intervals

class IntervalsAdapter(
    private val items: List<Intervals>,
    private val listener: ListActionListener
) :
    RecyclerView.Adapter<IntervalsAdapter.ViewHolder>() {
    private val selectedItems: MutableSet<Int> = mutableSetOf()
    private var isSelectionMode = false

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.intervalsTitle)
        val quantity: TextView = view.findViewById(R.id.intervalsQuantity)
        val btn: ImageButton = view.findViewById(R.id.intervalsBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntervalsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.intervals_item, parent, false)
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

    override fun onBindViewHolder(holder: IntervalsAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            title.text = item.title
            quantity.text = item.quantity.toString()
            itemView.apply {
                isSelected = selectedItems.contains(position)
                setBackgroundColor(
                    if (selectedItems.contains(position)) Color.LTGRAY else Color.WHITE
                )
                btn.setOnClickListener {
                    pressAnimation(it)
                    val intent = Intent(context, IntervalsViewActivity::class.java)
                    intent.putExtra("EXTRA_INTERVALS_ID_LONG", item.id)
                    context.startActivity(intent)
                }

                setOnLongClickListener {
                    selectionMode(position)
                    notifyItemChanged(position)
                    true
                }

                setOnClickListener {
                    if (isSelectionMode) {
                        selectionMode(position)
                        notifyItemChanged(position)
                    } else {
                        val intent = Intent(context, IntervalsViewActivity::class.java)
                        intent.putExtra("EXTRA_INTERVALS_ID_LONG", item.id)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun getSelectedItems(): List<Intervals> {
        return items.filterIndexed { index, _ -> selectedItems.contains(index) }
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyItemRangeChanged(0, itemCount)
    }

}