package diploma.work.magnum_opus.receiver

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import diploma.work.magnum_opus.R
import diploma.work.magnum_opus.RepetitionActivity

class NotificationReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent!!.getLongExtra("EXTRA_ID_LONG", -1)
        if (id == -1L) return
        val repetitionIntent = Intent(context, RepetitionActivity::class.java).apply {
            putExtra("EXTRA_ID_LONG", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val code = (id % Int.MAX_VALUE).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            code,
            repetitionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context!!, "MagnumOpus")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("MagnumOpus")
            .setContentText("Пришло время повторить материал $id")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(code, builder.build())
    }
}