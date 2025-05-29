package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.load
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.RepetitionActivity.Companion.setAlarm
import diploma.work.magnum_opus.databinding.ActivityAdditionBinding
import diploma.work.magnum_opus.db.DatabaseHelper
import diploma.work.magnum_opus.model.Intervals
import diploma.work.magnum_opus.model.Repetition
import diploma.work.magnum_opus.model.StudyMaterial


@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityAdditionBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityAdditionBinding must not be null")

class AdditionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        null_binding = ActivityAdditionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = if (ime.bottom < systemBars.bottom) {
                systemBars.bottom + ime.bottom
            } else {
                ime.bottom
            }
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        val db = DatabaseHelper(this@AdditionActivity)
        var intervalsId = 1L
        val idEdit = intent.getLongExtra("EXTRA_ID_LONG", -1L)
        val materialEdit: StudyMaterial? = if (idEdit != -1L)
            db.getMaterialById(idEdit)
        else
            null
        with(binding) {
            addBtnLeft.setOnClickListener {
                val intent = Intent(this@AdditionActivity, MainActivity::class.java)
                startActivity(intent)
            }
            btnAdd.setOnClickListener {
                pressAnimation(it)
                val material = StudyMaterial(
                    id = materialEdit?.id,
                    intervalsId = intervalsId,
                    title = additionTitle.text.toString(),
                    content = multiLineEditText.text.toString(),
                    createdAt = System.currentTimeMillis(),
                    isCompleted = materialEdit?.isCompleted ?: false
                )
                if (materialEdit == null) {
                    val id = db.saveMaterial(material)!!
                    val delay = db.getIntervalDelay(material.intervalsId, 1)!!
                    val delayInMillis = delay * 60 * 1000
                    val triggerTime = System.currentTimeMillis() + delayInMillis
                    setAlarm(this@AdditionActivity, id, triggerTime)
                    val repetition = Repetition(
                        materialId = id,
                        number = 1,
                        timestamp = triggerTime,
                        valuation = -1
                    )
                    db.saveRepetition(repetition)!!
                } else {
                    db.updateMaterial(material)
                }
                db.close()
                val intent = Intent(this@AdditionActivity, MainActivity::class.java)
                startActivity(intent)
            }
            val listIntervals = db.getIntervalsList()
            val spinAdapter = ArrayAdapter(
                this@AdditionActivity,
                android.R.layout.simple_spinner_item,
                listIntervals
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            addSpin.apply {
                adapter = spinAdapter
                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedItem = parent.getItemAtPosition(position) as Intervals
                        intervalsId = selectedItem.id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {

                    }
                }
            }
            addBtnAttach.setOnClickListener {
                imagePicker.launch("image/*")
            }
            if (materialEdit != null) {
                additionTitle.setText(materialEdit.title)
                multiLineEditText.setText(materialEdit.content)
                addSpin.setSelection(listIntervals.indexOfFirst { i -> i.id == materialEdit.intervalsId })
                btnAdd.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this@AdditionActivity,
                        R.drawable.ic_confirmation
                    )
                )
            } else
                btnAdd.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this@AdditionActivity,
                        R.drawable.ic_add
                    )
                )
        }
    }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                binding.additionImage.load(uri)
            }
        }
}