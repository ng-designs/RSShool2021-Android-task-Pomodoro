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

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.timePicker.setOnClickListener {

            dialogBinding = SetTimerValueDialogBinding.inflate(this.layoutInflater)

            val d: AlertDialog.Builder = AlertDialog.Builder(this)
            d.setTitle("Set timer value")
            d.setView(dialogBinding.root)

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

            d.setPositiveButton("Done"
            ) { _, _ ->
                run {
                    (dialogBinding.hoursPicker.value.toString() + ":" +
                            dialogBinding.minutesPicker.value.toString() + ":" +
                            dialogBinding.secondsPicker.value.toString()).also {
                        binding.timePicker.text = it
                    }
                }
            }
            d.setNegativeButton("Cancel"
            ) { _, _ -> }
            val alertDialog: AlertDialog = d.create()

            alertDialog.show()
        }

        binding.addNewStopwatchButton.setOnClickListener {
            if (binding.timePicker.text.isNotEmpty()) {
                val time = binding.timePicker.text.trim().split(":")

                val mills = ((time[0].toLong() * 60L) + (time[1].toLong() * 60L) + time[2].toLong()) * 1000L

                if (stopwatches.size <= 10) {
                    stopwatches.add(Stopwatch(nextId++, mills, mills, false))
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else Toast.makeText(this, "Timers limit is 10", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "Choose timer period", Toast.LENGTH_LONG).show()
        }

//        binding.addNewStopwatchButton.setOnClickListener {
//            stopwatches.add(Stopwatch(nextId++, 0, false))
//            stopwatchAdapter.submitList(stopwatches.toList())
//        }
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
}