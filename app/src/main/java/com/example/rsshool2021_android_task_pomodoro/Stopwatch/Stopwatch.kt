package com.example.rsshool2021_android_task_pomodoro.Stopwatch

data class Stopwatch(
    val id: Int,
    val startPeriod: Long,
    var currentMs: Long,
    var isStarted: Boolean
)