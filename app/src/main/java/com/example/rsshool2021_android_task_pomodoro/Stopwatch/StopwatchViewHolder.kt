package com.example.rsshool2021_android_task_pomodoro.Stopwatch

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool2021_android_task_pomodoro.R
import com.example.rsshool2021_android_task_pomodoro.databinding.StopwatchElementBinding

class StopwatchViewHolder(
    private val binding: StopwatchElementBinding,
    private val listener: StopwatchListener,
    private val resources: Resources

) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()

        binding.progressIndicator.setPeriod(stopwatch.startPeriod)

        when {
            stopwatch.currentMs <= 0L -> binding.progressIndicator.setCurrent(0)
            stopwatch.currentMs == stopwatch.startPeriod -> {
//                changeBackgroundOfItem(R.color.transparent)
                binding.progressIndicator.setCurrent(stopwatch.currentMs)
            }
        }

        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun startTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_pause_24, null)


        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun stopTimer(stopwatch: Stopwatch) {
        val drawable = resources.getDrawable(R.drawable.ic_baseline_play_arrow_24, null)
        binding.startPauseButton.setImageDrawable(drawable)

        stopwatch.isStarted = false
        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(PERIOD, UNIT_ONE_SEC) {

            override fun onTick(millisUntilFinished: Long) {
                if(stopwatch.isStarted){
                    stopwatch.currentMs -= UNIT_ONE_SEC
                    binding.progressIndicator.setCurrent(stopwatch.startPeriod - stopwatch.currentMs)
                    binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                }
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
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
//        private const val UNIT_TEN_MS = 10L
        private const val UNIT_ONE_SEC = 1000L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}