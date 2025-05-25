package diploma.work.magnum_opus.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import androidx.recyclerview.widget.RecyclerView
import diploma.work.magnum_opus.IntervalsViewActivity
import diploma.work.magnum_opus.R
import diploma.work.magnum_opus.model.Intervals

class IntervalsAdapter(private val items: List<Intervals>) :
    RecyclerView.Adapter<IntervalsAdapter.ViewHolder>() {

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

    override fun onBindViewHolder(holder: IntervalsAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.apply {
            title.text = item.title
            quantity.text = item.quantity.toString()
            itemView.apply {
                btn.setOnClickListener {
                    pressAnimation(it)
                    val intent = Intent(context, IntervalsViewActivity::class.java)
                    intent.putExtra("EXTRA_INTERVALS_ID_LONG", item.id)
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount() = items.size

}