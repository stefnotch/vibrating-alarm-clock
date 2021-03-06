package com.github.stefnotch.vibratingalarmclock

import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import com.github.stefnotch.vibratingalarmclock.data.DaysOfTheWeek
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    val args: SecondFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_cancel).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        val daysDisplay = view.findViewById<LinearLayout>(R.id.is_recurring_days)
        val okButton = view.findViewById<Button>(R.id.button_ok)
        val textInput = view.findViewById<TextInputEditText>(R.id.title_input)

        lifecycleScope.launch {
            val alarmRepository = AlarmRepository(requireContext())
            var isNewAlarm = false
            val alarm =
                if (args.alarmId == -1) Alarm(LocalTime.now()).let { isNewAlarm = true; it } else (alarmRepository.get(
                    args.alarmId
                ) ?: Alarm(LocalTime.now()).let { isNewAlarm = true; it })

            textInput.setText(alarm.title)
            okButton.isEnabled = !textInput.text?.toString().isNullOrBlank()
            textInput.doAfterTextChanged {
                okButton.isEnabled = !it?.toString().isNullOrBlank()
            }

            val timeInput = view.findViewById<Button>(R.id.time_input)
            timeInput.setOnClickListener {
                val timePicker = MaterialTimePicker.Builder()
                    .setTimeFormat(if(DateFormat.is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                    .setHour(alarm.time.hour)
                    .setMinute(alarm.time.minute)
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    alarm.time = alarm.time.withHour(timePicker.hour).withMinute(timePicker.minute)
                    timeInput.text = alarm.getFormattedTime(requireContext())
                }

                timePicker.show(requireActivity().supportFragmentManager, "time_input_tag")
            }

            timeInput.text = alarm.getFormattedTime(requireContext())

            val isRecurringInput = view.findViewById<CheckBox>(R.id.is_recurring)
            isRecurringInput.isChecked = alarm.isRecurring
            daysDisplay.visibility = if (alarm.isRecurring) LinearLayout.VISIBLE else LinearLayout.INVISIBLE
            isRecurringInput.setOnCheckedChangeListener { _, isChecked ->
                alarm.isRecurring = isChecked
                daysDisplay.visibility = if (alarm.isRecurring) LinearLayout.VISIBLE else LinearLayout.INVISIBLE
            }

            for ((id, day) in arrayOf(
                Pair(R.id.on_monday, DaysOfTheWeek.Monday),
                Pair(R.id.on_tuesday, DaysOfTheWeek.Tuesday),
                Pair(R.id.on_wednesday, DaysOfTheWeek.Wednesday),
                Pair(R.id.on_thursday, DaysOfTheWeek.Thursday),
                Pair(R.id.on_friday, DaysOfTheWeek.Friday),
                Pair(R.id.on_saturday, DaysOfTheWeek.Saturday),
                Pair(R.id.on_sunday, DaysOfTheWeek.Sunday),
            )) {
                val checkbox = view.findViewById<CheckBox>(id)
                checkbox.isChecked = DaysOfTheWeek.contains(alarm.days, day)
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    alarm.days = if (isChecked) alarm.days or day else alarm.days and day.inv()
                }
            }

            okButton.setOnClickListener {
                lifecycleScope.launch {
                    alarm.title = textInput.text.toString()
                    alarm.scheduleAlarm(requireContext())
                    if(isNewAlarm) {
                        alarmRepository.insert(alarm)
                    } else {
                        alarmRepository.update(alarm)
                    }
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
        }
    }
}