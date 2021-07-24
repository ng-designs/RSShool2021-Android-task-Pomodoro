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
import com.example.rsshool2021_android_task_pomodoro.Services.*
import com.example.rsshool2021_android_task_pomodoro.Stopwatch.Stopwatch
import com.example.rsshool2021_android_task_pomodoro.Stopwatch.StopwatchAdapter
import com.example.rsshool2021_android_task_pomodoro.Stopwatch.StopwatchListener
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.example.rsshool2021_android_task_pomodoro.databinding.SetTimerValueDialogBinding


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver, NumberPicker.OnValueChangeListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: SetTimerValueDialogBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {

            nextId = savedInstanceState.getInt(NEXT_ID)
            val size = savedInstanceState.getInt(TIMERS_COUNT)
            for (i in 0 until size) {
                val id = savedInstanceState.getInt("$ID$i")
                val initial = savedInstanceState.getLong("$INITIAL_VALUE$i")
                val currentMs = savedInstanceState.getLong("$VALUE_MS$i")
                val isStarted = savedInstanceState.getBoolean("$START$i")
                stopwatches.add(Stopwatch(id, initial, currentMs, isStarted))
            }
            stopwatchAdapter.submitList(stopwatches.toList())
        }

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.timerValueInput.setOnClickListener {

            dialogBinding = SetTimerValueDialogBinding.inflate(this.layoutInflater)

            val valueSetDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            valueSetDialog.setTitle("Set timer value")
            valueSetDialog.setView(dialogBinding.root)

            with(dialogBinding){

                hoursPicker.maxValue = 23
                hoursPicker.minValue = 0
                minutesPicker.minValue = 0
                minutesPicker.maxValue = 59
                secondsPicker.minValue = 0
                secondsPicker.maxValue = 59

                hoursPicker.setOnValueChangedListener { _, _, _ -> Log.d( TAG,"onValueChange: " ) }
                minutesPicker.setOnValueChangedListener { _, _, _ -> Log.d( TAG,"onValueChange: " ) }
                secondsPicker.setOnValueChangedListener { _, _, _ -> Log.d( TAG,"onValueChange: " ) }

            }

            valueSetDialog.setPositiveButton("Done" ) { _, _ ->
                run {
                    (dialogBinding.hoursPicker.value.toString() + ":" +
                            dialogBinding.minutesPicker.value.toString() + ":" +
                            dialogBinding.secondsPicker.value.toString()).also {
                        binding.timerValueInput.text = it
                    }
                }
            }
            valueSetDialog.setNegativeButton("Cancel" ) { _, _ -> }
            valueSetDialog.create().show()
        }

        binding.addNewStopwatchButton.setOnClickListener {
            if (binding.timerValueInput.text.isNotEmpty()) {
                val time = binding.timerValueInput.text.trim().split(":")

                val mills = ((time[0].toLong() * 60L) + (time[1].toLong() * 60L) + time[2].toLong()) * 1000L

                if (stopwatches.size <= 10) {
                    stopwatches.add(Stopwatch(nextId++, mills, mills, false))
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else Toast.makeText(this, "Timers limit reached(10)", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "Please set timer value", Toast.LENGTH_LONG).show()
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {

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
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(Stopwatch(it.id, it.startPeriod,currentMs ?: it.currentMs, isStarted))
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(NEXT_ID, nextId)
        outState.putInt(TIMERS_COUNT, stopwatches.size)

        for (i in stopwatches.indices) {
            outState.putInt("$ID$i", stopwatches[i].id)
            outState.putLong("$INITIAL_VALUE$i", stopwatches[i].startPeriod)
            outState.putLong("$VALUE_MS$i", stopwatches[i].currentMs)
            outState.putBoolean("$START$i", stopwatches[i].isStarted)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val runningTimer = stopwatches.find { it.isStarted }?.let { stopwatches[it.id] }

        if (runningTimer != null) {
            val startIntent = Intent(this, TimerService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(TIMER_LAST_VALUE_MS, runningTimer.currentMs) //startTime runningTimer.currentMs
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

    private companion object {
        private const val NEXT_ID = "NEXT_ID"
        private const val TIMERS_COUNT = "TIMERS_COUNT"
        private const val ID = "ID"
        private const val VALUE_MS = "VALUE_MS"
        private const val INITIAL_VALUE = "INITIAL_VALUE"
        private const val START = "START"
    }
}