package com.uninsubria.pillskeeper

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.*

class MyService : Service() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val f = intent!!.getSerializableExtra("f") as Farmaco
        val h = f.time.split(":")[0]
        val m = f.time.split(":")[1]
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            h.toInt(),
            m.toInt(),
            0
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 31){
            if (!alarmManager.canScheduleExactAlarms()) { //da android 12 bisogna dare il permesso
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                startActivity(intent)
            }
            //Toast.makeText(this, "Può? " + alarmManager.canScheduleExactAlarms(), Toast.LENGTH_SHORT).show()
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        //intent.putExtra("tel", 331)
        intent.putExtra("nome", f.name)
        //mail medico
        val pendingIntent = PendingIntent.getForegroundService(this, calendar.timeInMillis.toInt(), intent,PendingIntent.FLAG_MUTABLE)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Toast.makeText(this, "Alarm is set at ${f.time}", Toast.LENGTH_SHORT).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}