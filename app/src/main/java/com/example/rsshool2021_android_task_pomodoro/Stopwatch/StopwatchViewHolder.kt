package com.example.rsshool2021_android_task_pomodoro.Stopwatch

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool2021_android_task_pomodoro.MainActivity
import com.example.rsshool2021_android_task_pomodoro.R
import com.example.rsshool2021_android_task_pomodoro.databinding.StopwatchElementBinding

class StopwatchViewHolder(
    private val binding: StopwatchElementBinding,
    private val listener: StopwatchListener,
    private val resources: Resources

) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {

        with(binding){
            stopwatchTimer.text = stopwatch.currentMs.displayTime()
            progressIndicator.setPeriod(stopwatch.startPeriod)
            progressIndicator.setCurrent(stopwatch.startPeriod - stopwatch.currentMs)

            when {
                stopwatch.currentMs <= 0L -> {
                    progressIndicator.setCurrent(0)
                }
                stopwatch.currentMs == stopwatch.startPeriod -> {
                    progressIndicator.setCurrent(stopwatch.currentMs)
                }

            }

            if( stopwatch.isElapsed ) {
                timerCard.setBackgroundColor(resources.getColor(R.color.red_600_light))
                startPauseButton.text = resources.getString(R.string.button_reset)

            }else{
                timerCard.setBackgroundColor(resources.getColor(R.color.white))
            }
        }

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun initButtonsListeners(stopwatch: Stopwatch) {

        with(binding){
            startPauseButton.setOnClickListener {

                if(binding.startPauseButton.text.equals(resources.getString(R.string.button_reset))){
                    listener.reset(stopwatch.id)
                    stopwatch.isElapsed = false
                    timerCard.setBackgroundColor(resources.getColor(R.color.white))
                    binding.startPauseButton.text = resources.getString(R.string.button_start)
                }else {
                    if (stopwatch.isStarted) {
                        listener.stop(stopwatch.id, stopwatch.currentMs)
                    } else {
                        listener.start(stopwatch.id)
                    }
                }
            }

            deleteButton.setOnClickListener {
                stopTimer(stopwatch)
                listener.delete(stopwatch.id)
            }
        }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getString(R.string.button_stop)

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getString(R.string.button_start)

        timer?.cancel()
        stopwatch.isStarted = false

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_ONE_SEC) {

            override fun onTick(millisUntilFinished: Long) {
                when {
                    stopwatch.currentMs <= 0L -> onFinish()
                    stopwatch.isStarted -> {
                        stopwatch.currentMs -= UNIT_ONE_SEC
                        binding.progressIndicator.setCurrent(stopwatch.startPeriod - stopwatch.currentMs)
                        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                    }
                    else -> stopTimer(stopwatch)
                }
            }

            override fun onFinish() {
                binding.timerCard.setBackgroundColor(resources.getColor(R.color.red_600_light))
                stopwatch.isElapsed = true
                timer?.cancel()
                stopTimer(stopwatch)

                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                Toast.makeText(itemView.context, "Timer ${stopwatch.startPeriod.displayTime()} is elapsed!", Toast.LENGTH_LONG).show()

                binding.startPauseButton.text = resources.getString(R.string.button_reset)
            }
        }
    }

    private fun Long.displayTime(): String {
        if (this <= 0L) { return START_TIME }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val START_TIME = "00:00:00"
        private const val UNIT_ONE_SEC = 1000L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}