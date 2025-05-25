package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import diploma.work.magnum_opus.adapter.IntervalsAdapter
import diploma.work.magnum_opus.adapter.ListActionListener
import diploma.work.magnum_opus.databinding.ActivityIntervalsBinding
import diploma.work.magnum_opus.db.DatabaseHelper
import diploma.work.magnum_opus.dialog.CreateIntervalsDialog
import diploma.work.magnum_opus.model.Intervals
import java.util.Locale

@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityIntervalsBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityIntervalsBinding must not be null")

class IntervalsActivity : AppCompatActivity(), ListActionListener,
    CreateIntervalsDialog.OnIntervalsCreatedListener {
    private val db = DatabaseHelper(this@IntervalsActivity)
    private lateinit var items: MutableList<Intervals>

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
        items = db.getIntervalsList()
        val intervalsAdapter = IntervalsAdapter(items, this@IntervalsActivity)
        with(binding) {
            intervalsList.apply {
                layoutManager = LinearLayoutManager(this@IntervalsActivity)
                adapter = intervalsAdapter
            }
            interBtnLeft.setOnClickListener {
                pressAnimation(it)
                val intent = Intent(this@IntervalsActivity, MainActivity::class.java)
                startActivity(intent)
            }
            interBtnNew.setOnClickListener {
                pressAnimation(it)
                val dialog = CreateIntervalsDialog()
                dialog.show(supportFragmentManager, "createIntervalsDialog")
            }
            intervalsBtnDel.setOnClickListener {
                val selectedItems = intervalsAdapter.getSelectedItems()
                for (item in selectedItems) {
                    val index = items.indexOf(item)
                    if (index != -1) {
                        items.removeAt(index)
                        intervalsAdapter.notifyItemRemoved(index)
                    }
                    db.deleteIntervals(item.id)
                }
                intervalsAdapter.clearSelection()
                isSelectionMode(false)
            }

            interBtnCancel.setOnClickListener {
                intervalsAdapter.clearSelection()
                isSelectionMode(false)
            }
        }
    }

    override fun onIntervalsCreated(title: String) {
        val id = db.saveIntervals(title, null)
        if (id == null)
            Toast.makeText(
                this,
                "Ошибка, набор интервалов не был создан",
                Toast.LENGTH_SHORT
            ).show()
        else {
            db.saveInter(id, 1, 0L)
            items.add(
                Intervals(
                    id = id,
                    title = title,
                    quantity = 0,
                    desc = null
                )
            )
            val adapter = binding.intervalsList.adapter as IntervalsAdapter
            adapter.notifyItemInserted(items.size - 1)
            val intent = Intent(this, IntervalsViewActivity::class.java)
            intent.putExtra("EXTRA_INTERVALS_ID_LONG", id)
            startActivity(intent)
        }
    }

    override fun isSelectionMode(isSelectionMode: Boolean) {
        with(binding) {
            if (isSelectionMode) {
                intervalsContainer.visibility = View.INVISIBLE
                interContainerAlt.visibility = View.VISIBLE
            } else {
                intervalsContainer.visibility = View.VISIBLE
                interContainerAlt.visibility = View.GONE
            }
        }
    }

    override fun changeCounter(count: Int) {
        binding.intervalsCounter.text = String.format(Locale.getDefault(), "%d", count)
    }
}