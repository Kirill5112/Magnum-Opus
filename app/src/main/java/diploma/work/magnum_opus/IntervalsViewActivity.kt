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
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.databinding.ActivityIntervalsViewBinding
import diploma.work.magnum_opus.db.DatabaseHelper

@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityIntervalsViewBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityIntervalsViewBinding must not be null")

class IntervalsViewActivity : AppCompatActivity() {
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

        val id = intent.getLongExtra("EXTRA_INTERVALS_ID_LONG", -1L)
        if (id == -1L) {
            Toast.makeText(this, "Ошибка, нет id", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, IntervalsActivity::class.java)
            startActivity(intent)
        }
        val intervals = db.getIntervalsObject(id)!!
        with(binding) {
            intervalsViewTitle.text = intervals.title
            interViewCount.text = getCountText(intervals.quantity)
            intervalsViewBtnLeft.setOnClickListener {
                pressAnimation(it)
                val intent = Intent(this@IntervalsViewActivity, IntervalsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun getCountText(count: Int): String {
        return when {
            count == 1 -> "1 интервал"
            count == 0 -> "0 интервалов"
            count >= 5 -> "$count интервалов"
            count in 2..4 -> "$count интервала"
            else -> "ошибка"
        }
    }
}