package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.RepetitionActivity.Companion.cancelAlarm
import diploma.work.magnum_opus.adapter.RepetitionAdapter
import diploma.work.magnum_opus.databinding.ActivityMaterialViewBinding
import diploma.work.magnum_opus.db.DatabaseHelper
import diploma.work.magnum_opus.model.StudyMaterial

@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityMaterialViewBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityMaterialViewBinding must not be null")

class MaterialViewActivity : AppCompatActivity() {

    companion object {
        private const val ID_REMOVE = 1
        private const val ID_EDIT = 2

        fun deleteMaterial(context: Context, id: Long) {
            val db = DatabaseHelper(context)
            db.deleteMaterial(id)
            db.close()
            cancelAlarm(context, id)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        null_binding = ActivityMaterialViewBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val id = intent.getLongExtra("EXTRA_ID_LONG", -1L)
        val db = DatabaseHelper(this@MaterialViewActivity)
        if (id == -1L) {
            binding.matViewTitle.text = "нет id"
            return
        }
        val material: StudyMaterial = db.getMaterialById(id)!!
        val items = db.getRepetitionsList(id)
        val windowInsets = ViewCompat.getRootWindowInsets(binding.main)
        with(binding) {
            matViewTitle.text = material.title
            more.setOnClickListener {
                pressAnimation(it)
                showPopupMenu(more, material)
            }
            mvList.apply {
                layoutManager = LinearLayoutManager(this@MaterialViewActivity)
                adapter = RepetitionAdapter(items, material.content)
                applyWindowInsetsToRecyclerView(mvList, windowInsets)
            }
            mvBtnLeft.setOnClickListener {
                pressAnimation(it)
                val intent = Intent(this@MaterialViewActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showPopupMenu(view: View, material: StudyMaterial) {
        val popupMenu = PopupMenu(view.context, view)

        popupMenu.menu.add(0, ID_REMOVE, Menu.NONE, "Удалить")
        popupMenu.menu.add(0, ID_EDIT, Menu.NONE, "Редактировать")
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_REMOVE -> {
                    deleteMaterial(this@MaterialViewActivity, material.id!!)
                    val intent = Intent(this@MaterialViewActivity, MainActivity::class.java)
                    startActivity(intent)
                }

                ID_EDIT -> {
                    if (!material.isCompleted)
                        editMaterial(material.id!!)
                    else
                        Toast.makeText(
                            this,
                            "Повторение материала уже завершено",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }


    private fun editMaterial(id: Long) {

        val intent = Intent(this@MaterialViewActivity, AdditionActivity::class.java)
        intent.putExtra("EXTRA_ID_LONG", id)
        startActivity(intent)
    }

    private fun applyWindowInsetsToRecyclerView(
        recyclerView: RecyclerView,
        windowInsets: WindowInsetsCompat?
    ) {
        windowInsets?.let {
            val systemBars = it.getInsets(WindowInsetsCompat.Type.systemBars())
            recyclerView.updatePadding(bottom = systemBars.bottom)
        }
    }

}