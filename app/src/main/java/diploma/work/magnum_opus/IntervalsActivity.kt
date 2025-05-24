package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import diploma.work.magnum_opus.adapter.IntervalsAdapter
import diploma.work.magnum_opus.databinding.ActivityIntervalsBinding
import diploma.work.magnum_opus.db.DatabaseHelper

@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityIntervalsBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityIntervalsBinding must not be null")

class IntervalsActivity : AppCompatActivity() {
    private val db = DatabaseHelper(this@IntervalsActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        null_binding = ActivityIntervalsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val items = db.getIntervalsList()
        val intervalsAdapter = IntervalsAdapter(items)
        with(binding) {
            interList.apply {
                layoutManager = LinearLayoutManager(this@IntervalsActivity)
                adapter = intervalsAdapter
            }
            interBtnLeft.setOnClickListener {
                pressAnimation(it)
                val intent = Intent(this@IntervalsActivity, MainActivity::class.java)
                startActivity(intent)
            }
            interBtnNew.setOnClickListener{
                pressAnimation(it)

            }
        }
    }
}