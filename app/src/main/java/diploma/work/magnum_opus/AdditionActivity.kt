package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.RepetitionActivity.Companion.setAlarm
import diploma.work.magnum_opus.databinding.ActivityAdditionBinding
import diploma.work.magnum_opus.db.DatabaseHelper
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
        val idEdit = intent.getLongExtra("EXTRA_ID_LONG", -1L)
        val materialEdit: StudyMaterial? = if (idEdit != -1L)
            db.getMaterialById(idEdit)
        else
            null
        with(binding) {
            if (materialEdit != null) {
                additionTitle.setText(materialEdit.title)
                multiLineEditText.setText(materialEdit.content)
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
            addBtnLeft.setOnClickListener {
                val intent = Intent(this@AdditionActivity, MainActivity::class.java)
                startActivity(intent)
            }
            btnAdd.setOnClickListener { it ->
                pressAnimation(it)
                val material = StudyMaterial(
                    id = materialEdit?.id,
                    title = additionTitle.text.toString(),
                    content = multiLineEditText.text.toString(),
                    createdAt = System.currentTimeMillis(),
                    isCompleted = materialEdit?.isCompleted ?: false
                )
                if (materialEdit == null) {
                    val id = db.saveMaterial(material)!!
                    val delay =
                        this@AdditionActivity.resources.getIntArray(R.array.repetition_delays)
                            .map { it.toLong() }.first()
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
        }
    }
}