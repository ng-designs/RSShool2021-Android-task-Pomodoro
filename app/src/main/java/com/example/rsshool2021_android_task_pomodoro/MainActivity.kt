package com.example.rsshool2021_android_task_pomodoro


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
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
import java.util.*


class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver, NumberPicker.OnValueChangeListener {

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

        with(binding){
            hoursPicker.minValue = 0
            hoursPicker.maxValue = 23
            minutesPicker.minValue = 0
            minutesPicker.maxValue = 59
            secondsPicker.maxValue = 0
            secondsPicker.maxValue = 59

            hoursPicker.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })
            minutesPicker.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })
            secondsPicker.setFormatter(NumberPicker.Formatter { i -> String.format("%02d", i) })
        }

        binding.hoursPicker.setOnValueChangedListener(this)
        binding.minutesPicker.setOnValueChangedListener(this)
        binding.secondsPicker.setOnValueChangedListener(this)





//        binding.timePicker.setOnClickListener {
//            val c = Calendar.getInstance()
//            val hour = c.get(Calendar.MINUTE)
//            val minute = c.get(Calendar.SECOND)
//            val timePickerDialog =
//                TimePickerDialog(this, TimePickerDialog.THEME_HOLO_DARK, { _, h, m ->
//                    binding.timePicker.text = resources.getString(R.string.time, h, m)
//                }, hour, minute, true)
//
//            timePickerDialog.show()
//        }
//
//        binding.addNewStopwatchButton.setOnClickListener {
//            if (binding.timePicker.text.isNotEmpty()) {
//                val time = binding.timePicker.text.trim()
//                val h = time.subSequence(0, time.indexOf(":")).toString()
//                val m = time.subSequence(time.indexOf(":") + 1, time.length).toString()
//                val mills = (h.toInt() * 60L + m.toInt()) * 60L * 1000L
//                if (stopwatches.size <= 10) {
//                    stopwatches.add(Stopwatch(nextId++, mills, mills, false))
//                    stopwatchAdapter.submitList(stopwatches.toList())
//                } else Toast.makeText(this, "Timers limit is 10", Toast.LENGTH_LONG).show()
//            } else Toast.makeText(this, "Choose timer period", Toast.LENGTH_LONG).show()
//        }

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
            startIntent.putExtra(
                STARTED_TIMER_TIME_MS,
                runningTimer.currentMs
            ) //startTime runningTimer.currentMs
            startIntent.putExtra("System time", System.currentTimeMillis())
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