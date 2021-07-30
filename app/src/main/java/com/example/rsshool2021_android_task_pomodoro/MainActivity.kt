package com.example.rsshool2021_android_task_pomodoro

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool2021_android_task_pomodoro.Dialogs.*
import com.example.rsshool2021_android_task_pomodoro.Services.*
import com.example.rsshool2021_android_task_pomodoro.Stopwatch.Stopwatch
import com.example.rsshool2021_android_task_pomodoro.Stopwatch.StopwatchAdapter
import com.example.rsshool2021_android_task_pomodoro.Stopwatch.StopwatchListener
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.example.rsshool2021_android_task_pomodoro.databinding.SetTimerValueDialogBinding


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.timerValueInput.setOnClickListener {
            TimerValueSetDialog(this, binding.timerValueInput)
        }

        binding.addNewStopwatchButton.setOnClickListener {

            val pickerValues = binding.timerValueInput.text.trim().split(":")
            val time = pickerValues.sumOf { it.toInt() }

            if (time > 0) {
                val mills = (((pickerValues[0].toLong() * 60L) + pickerValues[1].toLong()) * 60L + pickerValues[2].toLong()) * 1000L

                if (stopwatches.size <= 20) {
                    stopwatches.add(Stopwatch(nextId++, mills, mills, false))
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else Toast.makeText(this, "Timers limit reached(20)", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "Please set timer value", Toast.LENGTH_LONG).show()
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun reset(id: Int) {
        changeStopwatch(id, 0L, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {

        stopwatches.find { it.isStarted && it.id != id && isStarted }?.let { it.isStarted = false }

        stopwatches.find { it.id == id }?.let {
            it.isStarted = isStarted
            if(currentMs != null) it.currentMs = currentMs
            if(currentMs?.toInt() == 0) it.currentMs = it.startPeriod
        }

        stopwatchAdapter.submitList(stopwatches.toMutableList())
        stopwatchAdapter.notifyDataSetChanged()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val activeTimer = stopwatches.find { it.isStarted }

        if (activeTimer != null) {
            val startIntent = Intent(this, TimerService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(TIMER_INITIAL_VALUE, activeTimer.startPeriod)
            startIntent.putExtra(TIMER_LAST_VALUE_MS, activeTimer.currentMs)
            startIntent.putExtra(LAST_SYSTEM_TIME, System.currentTimeMillis())
            startService(startIntent)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        try {
            val stopIntent = Intent(this, TimerService::class.java)
            stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
            startService(stopIntent)
        } catch (ex: Exception){
            Log.i("exception", ex.stackTraceToString())
        }
    }
}