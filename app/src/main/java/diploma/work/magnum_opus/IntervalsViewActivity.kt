package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.adapter.InterActionListener
import diploma.work.magnum_opus.adapter.InterAdapter
import diploma.work.magnum_opus.databinding.ActivityIntervalsViewBinding
import diploma.work.magnum_opus.db.DatabaseHelper
import kotlin.properties.Delegates

@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityIntervalsViewBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityIntervalsViewBinding must not be null")
private var id by Delegates.notNull<Long>()
private lateinit var delays: MutableList<Long>
private lateinit var interAdapter: InterAdapter
private val text
    get() = binding.interViewCount

class IntervalsViewActivity : AppCompatActivity(), InterActionListener {
    private val db = DatabaseHelper(this@IntervalsViewActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        null_binding = ActivityIntervalsViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        id = intent.getLongExtra("EXTRA_INTERVALS_ID_LONG", -1L)
        if (id == -1L) {
            Toast.makeText(this, "Ошибка, нет id", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, IntervalsActivity::class.java)
            startActivity(intent)
        }
        val intervals = db.getIntervalsObject(id)!!
        delays = db.getDelayList(id)
        interAdapter = InterAdapter(delays, this@IntervalsViewActivity)
        with(binding) {
            intervalsViewTitle.text = intervals.title
            interViewCount.text = getDeclinationText(intervals.quantity)
            interList.apply {
                layoutManager = LinearLayoutManager(this@IntervalsViewActivity)
                adapter = interAdapter
            }
            intervalsViewBtnLeft.setOnClickListener {
                pressAnimation(it)
                val intent = Intent(this@IntervalsViewActivity, IntervalsActivity::class.java)
                startActivity(intent)
            }
            interViewBtnNew.setOnClickListener {
                pressAnimation(it)
                delays.add(30L)
                interAdapter.apply {
                    notifyItemInserted(delays.size - 1)
                    notifyItemChanged(delays.size - 2)
                }
                db.saveInter(id, delays.size, 30L)
                intervals.quantity = delays.size
                interViewCount.text = getDeclinationText(delays.size)
            }
            intervalsViewInfo.setOnClickListener{
                pressAnimation(it)
            }
        }
    }

    private fun getDeclinationText(count: Int): String {
        return when {
            count == 1 -> "1 интервал"
            count == 0 -> "0 интервалов"
            count in 2..4 -> "$count интервала"
            count in 5..20 -> "$count интервалов"
            count > 20 && count % 10 == 1 -> "$count интервал"
            count > 20 && count % 10 in 2..4 -> "$count интервала"
            (count > 20 && count % 10 in 5..9) || count % 10 == 0 -> "$count интервалов"
            else -> "ошибка"
        }
    }

    override fun deleteInter(number: Int) {
        delays.removeAt(number - 1)
        interAdapter.notifyItemRemoved(number - 1)
        interAdapter.notifyItemChanged(number - 2)
        db.deleteIntervalDelay(id, number)
        text.text =  getDeclinationText(delays.size)
    }

    override fun saveInter(number: Int, delay: Long) {
        delays[number - 1] = delay
        interAdapter.notifyItemChanged(number - 1)
        db.saveInter(id, number, delay)
    }
}