package diploma.work.magnum_opus

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import diploma.work.magnum_opus.MainActivity.Companion.pressAnimation
import diploma.work.magnum_opus.databinding.ActivityRepetitionBinding
import diploma.work.magnum_opus.db.DatabaseHelper
import diploma.work.magnum_opus.model.Repetition
import diploma.work.magnum_opus.receiver.NotificationReceiver

@SuppressLint("StaticFieldLeak")
private var null_binding: ActivityRepetitionBinding? = null
private val binding
    get() = null_binding
        ?: throw IllegalStateException("Binding for ActivityRepetitionBinding must not be null")

class RepetitionActivity : AppCompatActivity() {

    companion object {
        private lateinit var alarmManager: AlarmManager
        fun setAlarm(context: Context, id: Long, time: Long) {
            alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("EXTRA_ID_LONG", id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (id % Int.MAX_VALUE).toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            } else
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }

        @SuppressLint("MissingPermission")
        fun cancelAlarm(context: Context, id: Long) {
            alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val code = (id % Int.MAX_VALUE).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(code)
        }
    }

    private var isHide = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        null_binding = ActivityRepetitionBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val id = intent.getLongExtra("EXTRA_ID_LONG", -1L)
        val db = DatabaseHelper(this)
        val material = db.getMaterialById(id)!!
        val notificationManager = NotificationManagerCompat.from(this@RepetitionActivity)
        if (material.isCompleted) {
            Toast.makeText(this, "последнее повторение уже было завершено", Toast.LENGTH_SHORT)
                .show()
            val intent1 = Intent(this@RepetitionActivity, MainActivity::class.java)
            startActivity(intent1)
        }
        notificationManager.cancel((id % Int.MAX_VALUE).toInt())
        with(binding) {
            repTitle.text = material.title
            repMultiLineTextView.text = material.content
            val text = "Вспомните как можно больше о '${material.title}'"
            repHt.text = text
            repShowHide.setOnClickListener {
                pressAnimation(it)
                isHide = isHide.not()
                val src = if (isHide) R.drawable.ic_eye_crossed else R.drawable.ic_eye
                repShowHide.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this@RepetitionActivity,
                        src
                    )
                )
                if (isHide) {
                    repContent.visibility = View.INVISIBLE
                    rephintT.visibility = View.VISIBLE
                } else {
                    repContent.visibility = View.VISIBLE
                    rephintT.visibility = View.INVISIBLE
                }
            }
            repSb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    repSbTv.text = "$progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })
            repBtnEnd.setOnClickListener {
                pressAnimation(it)
                notificationManager.cancel((id % Int.MAX_VALUE).toInt())
                val repetition = db.getLastRepetition(material.id!!)!!
                repetition.timestamp = System.currentTimeMillis()
                repetition.valuation = repSb.progress
                db.updateRepetition(repetition)
                val quantity = db.getQuantity(material.intervalsId)
                if (repetition.number >= quantity) {
                    Toast.makeText(
                        this@RepetitionActivity,
                        "Последнее повторение завершено",
                        Toast.LENGTH_SHORT
                    ).show()
                    material.isCompleted = true
                    db.updateMaterial(material)
                } else {
                    val delay = db.getIntervalDelay(material.intervalsId, repetition.number + 1)!!
                    val delayInMillis = delay * 60 * 1000
                    val triggerTime = System.currentTimeMillis() + delayInMillis
                    val newRepetition = Repetition(
                        materialId = repetition.materialId,
                        number = repetition.number + 1,
                        timestamp = triggerTime,
                        valuation = -1
                    )
                    db.saveRepetition(newRepetition)!!
                    setAlarm(this@RepetitionActivity, newRepetition.materialId, triggerTime)
                }
                db.close()
                val intent = Intent(this@RepetitionActivity, MainActivity::class.java)
                startActivity(intent)
            }
            repBtnLeft.setOnClickListener {
                pressAnimation(it)
                notificationManager.cancel((id % Int.MAX_VALUE).toInt())
                val intent = Intent(this@RepetitionActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}