package com.example.rsshool2021_android_task_pomodoro.Services

const val START_TIME = "00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val TIMER_INITIAL_VALUE = "TIMER_INITIAL_VALUE"
const val TIMER_LAST_VALUE_MS = "TIMER_LAST_VALUE_MS"
const val LAST_SYSTEM_TIME = "LAST_SYSTEM_TIME"

fun Long.displayTime(): String {
    if (this <= 0L) {
        return START_TIME
    }
    val h = this / 1000L / 3600L
    val m = this / 1000L % 3600L / 60L
    val s = this / 1000L % 60L
//    val ms = this % 1000 / 10

//    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}