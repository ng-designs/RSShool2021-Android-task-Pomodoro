package com.example.rsshool2021_android_task_pomodoro.Stopwatch

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)