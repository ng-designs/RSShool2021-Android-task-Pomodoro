package com.example.rsshool2021_android_task_pomodoro.Services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.rsshool2021_android_task_pomodoro.MainActivity
import com.example.rsshool2021_android_task_pomodoro.R
import kotlinx.coroutines.*

class TimerService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                val timerInitialValue = intent?.extras?.getLong(TIMER_INITIAL_VALUE) ?: return
                val timerLastValue = intent.extras?.getLong(TIMER_LAST_VALUE_MS) ?: return
                val lastSystemTime = intent.extras?.getLong(LAST_SYSTEM_TIME) ?: return
                commandStart(timerInitialValue, timerLastValue, lastSystemTime)
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart(timerInitialValue: Long, timerLastValue: Long, lastSystemTime: Long) {
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(timerInitialValue, timerLastValue, lastSystemTime)
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer(timerInitialValue: Long, timerLastValue: Long, lastSystemTime: Long) {
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                val time = timerLastValue - (System.currentTimeMillis() - lastSystemTime)
                if (time > 0) {
                    notificationManager?.notify(
                        NOTIFICATION_ID, getNotification( time.displayTime(), true)
                )
                    delay(INTERVAL)
                }else {
                    notificationManager?.notify(
                        NOTIFICATION_ID,
                        getNotification("(${timerInitialValue.displayTime()}) Timer Elapsed!", false)
                    )
                    break
                }
            }
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, TimerService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, TimerService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content",true)
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String, isSilent : Boolean): Notification {

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(isSilent)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
            .setContentText(content).build()
    }


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}