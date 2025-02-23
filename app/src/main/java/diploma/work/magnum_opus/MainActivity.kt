package diploma.work.magnum_opus

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.ScaleAnimation
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import diploma.work.magnum_opus.settings.AppPreferences.completedIsHide
import diploma.work.magnum_opus.MaterialViewActivity.Companion.deleteMaterial
import diploma.work.magnum_opus.adapter.MaterialAdapter
import diploma.work.magnum_opus.adapter.ListActionListener
import diploma.work.magnum_opus.databinding.ActivityMainBinding
import diploma.work.magnum_opus.db.DatabaseHelper
import diploma.work.magnum_opus.item.ItemOfMaterialAdapter
import diploma.work.magnum_opus.model.Repetition
import diploma.work.magnum_opus.model.StudyMaterial
import diploma.work.magnum_opus.settings.AppPreferences.sortId
import diploma.work.magnum_opus.settings.AppPreferences.sortIsIncreasing
import java.util.Locale


@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityMainBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityMainBinding must not be null")
private lateinit var materials: MutableList<StudyMaterial>
private lateinit var repetitions: MutableList<Repetition>
private lateinit var items: MutableList<ItemOfMaterialAdapter>

class MainActivity : AppCompatActivity(), ListActionListener {
    private val db = DatabaseHelper(this@MainActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        null_binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission()
        }

        createNotificationChannel()

        materials = db.getAllMaterials()
        repetitions = db.getAllLastRepetition()
        items = mutableListOf()
        for (material in materials) {
            val index = materials.indexOf(material)
            if (!completedIsHide || !material.isCompleted) {
                items.add(
                    ItemOfMaterialAdapter(
                        material.id!!,
                        material.title,
                        repetitions[index].timestamp,
                        material.isCompleted
                    )
                )
            }
        }
        with(binding) {
            btnNew.setOnClickListener {
                pressAnimation(it)
                val intent = Intent(this@MainActivity, AdditionActivity::class.java)
                startActivity(intent)
            }
            val materialAdapter = MaterialAdapter(items, this@MainActivity)
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = materialAdapter
            }
            btnDel.setOnClickListener {
                val selectedItems = materialAdapter.getSelectedItems()
                for (item in selectedItems) {
                    val index = items.indexOf(item)
                    if (index != -1) {
                        items.removeAt(index)
                        materialAdapter.notifyItemRemoved(index)
                        materials.removeAt(index)
                        repetitions.removeAt(index)
                    }
                    deleteMaterial(this@MainActivity, item.id)
                }
                materialAdapter.clearSelection()
                isSelectionMode(false)
            }
            btnLeft.setOnClickListener {
                materialAdapter.clearSelection()
                isSelectionMode(false)
            }
            /**
             * меню только для btnFilter кнопки
             */
            val filter =
                createPopupMenuFilter(btnFilter, materialAdapter)
            btnFilter.setOnClickListener {
                pressAnimation(it)
                filter.show()
            }
            btnMore.setOnClickListener {
                pressAnimation(it)
                showPopupMenuMore(it)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Toast.makeText(
                        this,
                        "Разрешение на уведомления предоставлено",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Разрешение на уведомления отклонено",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Magnum Opus"
            val description = "Канал уведомлений для Magnum Opus"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("MagnumOpus", name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun isSelectionMode(isSelectionMode: Boolean) {
        with(binding) {
            if (isSelectionMode) {
                btnNew.visibility = View.GONE
                mainTitle.visibility = View.GONE
                btnFilter.visibility = View.GONE
                btnDel.visibility = View.VISIBLE
                btnLeft.visibility = View.VISIBLE
                mainCounter.visibility = View.VISIBLE
            } else {
                btnNew.visibility = View.VISIBLE
                mainTitle.visibility = View.VISIBLE
                btnFilter.visibility = View.VISIBLE
                btnDel.visibility = View.GONE
                btnLeft.visibility = View.GONE
                mainCounter.visibility = View.GONE
            }
        }
    }

    override fun changeCounter(count: Int) {
        binding.mainCounter.text = String.format(Locale.getDefault(), "%d", count)
    }

    companion object {
        private const val ID_HIDE_COMPLETED = 1
        private const val ID_SORT = 2
        private const val ID_SORT_DATE = 3
        private const val ID_SORT_NAME = 4
        private const val ID_SETTINGS = 5

        fun pressAnimation(view: View) {
            val scaleAnimation = ScaleAnimation(
                0.85f,
                1.0f,
                0.85f,
                1.0f,
                ScaleAnimation.RELATIVE_TO_SELF,
                0.5f,
                ScaleAnimation.RELATIVE_TO_SELF,
                0.5f
            )
            scaleAnimation.duration = 100
            view.startAnimation(scaleAnimation)
        }
    }

    private fun createPopupMenuFilter(
        view: View, materialAdapter: MaterialAdapter
    ): PopupMenu {
        val popupMenu = PopupMenu(view.context, view)

        val hideCompletedItem =
            popupMenu.menu.add(1, ID_HIDE_COMPLETED, Menu.NONE, "Скрыть завершённые")
        hideCompletedItem.setCheckable(true)
        hideCompletedItem.setChecked(completedIsHide)

        val sortUp = AppCompatResources.getDrawable(this, R.drawable.ic_sort_up)
        val sortDown = AppCompatResources.getDrawable(this, R.drawable.ic_sort_down)
        val sortSubMenu = popupMenu.menu.addSubMenu(3, ID_SORT, Menu.NONE, "Сортировка")
        sortSubMenu.add(3, ID_SORT_DATE, Menu.NONE, "По дате")
        sortSubMenu.add(3, ID_SORT_NAME, Menu.NONE, "По названию")

        popupMenu.menu.findItem(sortId).icon = if (sortIsIncreasing) sortUp else sortDown
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_HIDE_COMPLETED -> {
                    completedIsHide = !it.isChecked
                    it.isChecked = completedIsHide
                    toggleHidingCompleted(materialAdapter)
                }

                ID_SORT_NAME -> {
                    if (setSortIconAndID(popupMenu, it)) reverseLists(materialAdapter)
                }

                ID_SORT_DATE -> {
                    if (setSortIconAndID(popupMenu, it)) reverseLists(materialAdapter)
                }
            }
            return@setOnMenuItemClickListener true
        }
        return popupMenu
    }

    private fun reverseLists(materialAdapter: MaterialAdapter) {
        materials.reverse()
        repetitions.reverse()
        items.reverse()
        materialAdapter.notifyItemRangeChanged(0, items.size)
    }

    /**
     * Этот метод устанавливает правильную иконку(возрастание/убывание), а также сохраняет настройки сортировки.
     * @param popupMenu меню
     * @param item нажатый элемент меню
     * @return возвращает true, если тип сортировки не изменился, иначе false
     */
    private fun setSortIconAndID(popupMenu: PopupMenu, item: MenuItem): Boolean {
        val sortUp = AppCompatResources.getDrawable(this, R.drawable.ic_sort_up)
        if (sortId == item.itemId) {
            val sortDown = AppCompatResources.getDrawable(this, R.drawable.ic_sort_down)
            item.icon = if (sortIsIncreasing) sortDown else sortUp
            sortIsIncreasing = !sortIsIncreasing
            return true
        } else {
            item.icon = sortUp
            sortIsIncreasing = true
            popupMenu.menu.findItem(sortId).icon = null
            sortId = item.itemId
            return false
        }
    }

    private fun sortByName(materialAdapter: MaterialAdapter) {
        materials.sortBy { it.title }
        for(i in materials.indices){
            val id = materials[i].id
        }
    }

    private fun toggleHidingCompleted(
        materialAdapter: MaterialAdapter
    ) {
        if (completedIsHide) {
            val iterator = items.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.isCompleted) {
                    val index = items.indexOf(item)
                    iterator.remove()
                    materialAdapter.notifyItemRemoved(index)
                }
            }
        } else {
            for (material in materials) {
                if (material.isCompleted) {
                    val index = materials.indexOf(material)
                    val repetition = repetitions[index]
                    val item = ItemOfMaterialAdapter(
                        material.id!!,
                        material.title,
                        repetition.timestamp,
                        material.isCompleted
                    )
                    if (!items.contains(item)) {
                        items.add(index, item)
                        materialAdapter.notifyItemInserted(index)
                    }
                }
            }
        }
    }

    private fun showPopupMenuMore(
        view: View
    ) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menu.add(2, ID_SETTINGS, Menu.NONE, getString(R.string.settings_label))
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                ID_SETTINGS -> {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }
}