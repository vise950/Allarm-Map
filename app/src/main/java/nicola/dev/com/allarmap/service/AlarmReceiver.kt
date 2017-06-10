package nicola.dev.com.allarmap.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.PowerManager
import nicola.dev.com.allarmap.utils.Constant
import nicola.dev.com.allarmap.utils.Utils


class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = "Alarm receiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "")
        wl.acquire()

        if (intent.action == Constant.START_ALARM) {
            context.startService(Intent(context, AlarmService::class.java))
        } else if (intent.action == Constant.STOP_ALARM) {
            if (Utils.isMyServiceRunning(context, AlarmService::class.java)) {
                context.stopService(Intent(context, AlarmService::class.java))
            }

            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(Constant.NOTIFICATION_ID)

            wl.release()
        }
    }
}