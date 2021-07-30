package com.example.rsshool2021_android_task_pomodoro.Dialogs

import android.content.ContentValues
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.rsshool2021_android_task_pomodoro.MainActivity
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.example.rsshool2021_android_task_pomodoro.databinding.SetTimerValueDialogBinding

class TimerValueSetDialog(private val activity: MainActivity, private val tview : TextView) : AlertDialog.Builder(activity) {
    private val dialogBinding = SetTimerValueDialogBinding.inflate(activity.layoutInflater)
    private val activityBinding = ActivityMainBinding.inflate(activity.layoutInflater)

    init {
        setTitle("Set timer value")
        setView(dialogBinding.root)

        with(dialogBinding) {

            hoursPicker.maxValue = 23
            hoursPicker.minValue = 0
            minutesPicker.minValue = 0
            minutesPicker.maxValue = 59
            secondsPicker.minValue = 0
            secondsPicker.maxValue = 59

            hoursPicker.setOnValueChangedListener { picker, oldVal, newVal -> Log.d( ContentValues.TAG, "onValueChange: " ) }
            minutesPicker.setOnValueChangedListener { _, _, _ -> Log.d( ContentValues.TAG, "onValueChange: " ) }
            secondsPicker.setOnValueChangedListener { _, _, _ -> Log.d( ContentValues.TAG,"onValueChange: " ) }
        }

        setPositiveButton("Done" ) { _, _ ->
            run {
                with(dialogBinding){
                    ((if(hoursPicker.value < 10) "0${hoursPicker.value}" else hoursPicker.value.toString()) + ":" +
                            (if(minutesPicker.value < 10) "0${minutesPicker.value}" else minutesPicker.value.toString()) + ":" +
                            (if(secondsPicker.value < 10) "0${secondsPicker.value}" else secondsPicker.value.toString())
                            ).also { tview.text = it }
                }
            }
        }
        setNegativeButton("Cancel" ) { _, _ -> }
        create()
        show()
    }
}