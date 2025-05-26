package diploma.work.magnum_opus.adapter

import android.annotation.SuppressLint
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.R
import androidx.core.view.isVisible

interface InterActionListener {
    fun deleteInter(number: Int)
    fun saveInter(number: Int, delay: Long)
}

class InterAdapter(
    private val items: List<Long>,
    private val listener: InterActionListener
) :
    RecyclerView.Adapter<InterAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.inter_item_text)
        val editBtn: ImageButton = view.findViewById(R.id.inter_item_edit)
        val deleteBtn: ImageButton = view.findViewById(R.id.inter_item_delete)
        val number: EditText = view.findViewById(R.id.inter_item_number)
        val spinner: Spinner = view.findViewById(R.id.inter_spin)
        val confirmBtn: ImageButton = view.findViewById(R.id.inter_item_confirm)
        val normal: LinearLayout = view.findViewById(R.id.inter_item_normal)
        val alt: LinearLayout = view.findViewById(R.id.inter_item_alt)
        val rounded: LinearLayout = view.findViewById(R.id.inter_item_rounded)
        val time: ImageView = view.findViewById(R.id.inter_item_time)
    }

    private var measureUnit = "Минута"
    private var isEditMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inter_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.apply {
            text.text = getDeclinationText(item)
            val ems = text.text.toString().split(" ")
            val pos = if (ems.size == 2) when (ems[1]) {
                "минута", "минут", "минуты" -> 0
                "час", "часов", "часа" -> 1
                "день", "дней", "дня" -> 2
                else -> 3
            }
            else
                0
            deleteBtn.setOnClickListener {
                pressAnimation(it)
                listener.deleteInter(adapterPosition + 1)
            }
            if (position != items.size - 1 || position == 0)
                deleteBtn.visibility = View.GONE
            else
                deleteBtn.visibility = View.VISIBLE
            editBtn.setOnClickListener {
                if (!isEditMode) {
                    isEditMode = true
                    pressAnimation(it)
                    invertVisibility(holder)
                    number.setText(if (ems.size == 2) ems[0] else "0")
                    spinner.setSelection(pos)
                }
            }
            itemView.apply {
                val shape = rounded.background as GradientDrawable
                val vector = time.drawable as VectorDrawable
                val textColor: Int
                when (pos) {
                    0 -> {
                        shape.setColor(ContextCompat.getColor(context, R.color.light_purple))
                        textColor = ContextCompat.getColor(context, R.color.light_purple_text)
                    }

                    1 -> {
                        shape.setColor(ContextCompat.getColor(context, R.color.light_blue))
                        textColor = ContextCompat.getColor(context, R.color.light_blue_text)
                    }

                    2 -> {
                        shape.setColor(ContextCompat.getColor(context, R.color.light_pink))
                        textColor = ContextCompat.getColor(context, R.color.light_pink_text)
                    }

                    else -> {
                        shape.setColor(ContextCompat.getColor(context, R.color.light_green))
                        textColor = ContextCompat.getColor(context, R.color.light_green_text)
                    }
                }
                text.setTextColor(textColor)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    time.drawable.colorFilter = BlendModeColorFilter(textColor, BlendMode.SRC_IN)
                } else {
                    @Suppress("DEPRECATION")
                    vector.mutate().setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
                }
                val spinAdapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_spinner_item,
                    arrayOf("Минута", "Час", "День", "Неделя")
                ).also {
                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinner.apply {
                    adapter = spinAdapter
                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedItem = parent.getItemAtPosition(position) as String
                            measureUnit = selectedItem
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                        }
                    }
                }

                confirmBtn.setOnClickListener {
                    pressAnimation(it)
                    val multiplayer =
                        when (measureUnit) {
                            "Минута" -> 1
                            "Час" -> 60
                            "День" -> 1440
                            "Неделя" -> 10_080
                            else -> 1
                        }
                    val delay = number.text.toString().toLongOrNull()?.times(multiplayer)
                    if (delay != null)
                        listener.saveInter(adapterPosition + 1, delay)
                    invertVisibility(holder)
                    isEditMode = false
                }
            }
        }
    }

    override fun getItemCount() = items.size

    private fun getDeclinationText(count: Long): String {
        if (count == 0L)
            return "сразу"
        val one: String
        val few: String
        val many: String
        val divider: Int
        when {
            count % 10_080 == 0L -> {
                one = "неделя"; few = "недели"; many = "недель"; divider = 10_080
            }

            count % 1_440 == 0L -> {
                one = "день"; few = "дня"; many = "дней"; divider = 1_440
            }

            count % 60 == 0L -> {
                one = "час"; few = "часа"; many = "часов"; divider = 60
            }

            else -> {
                one = "минута"; few = "минуты"; many = "минут"; divider = 1
            }
        }
        val res = count / divider
        return when {
            res == 1L -> "$res $one"
            res in 2..4 -> "$res $few"
            res in 5..20 -> "$res $many"
            res > 20 && res % 10 == 1L -> "$res $one"
            res > 20 && res % 10 in 2..4 -> "$res $few"
            (res > 20 && res % 10 in 5..9) || res % 10 == 0L -> "$res $many"
            else -> "ошибка"
        }
    }

    private fun invertVisibility(holder: ViewHolder) {
        holder.apply {
            if (normal.isVisible) {
                normal.visibility = View.GONE
                alt.visibility = View.VISIBLE
            } else {
                normal.visibility = View.VISIBLE
                alt.visibility = View.GONE
            }
        }
    }
}