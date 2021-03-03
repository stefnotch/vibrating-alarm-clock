package com.github.stefnotch.vibratingalarmclock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
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

        val okButton = view.findViewById<Button>(R.id.button_ok)
        val textInput = view.findViewById<TextInputEditText>(R.id.title_input)

        lifecycleScope.launch {
            val alarmRepository = AlarmRepository(requireContext())
            val alarm =
                if (args.alarmId == -1) alarmRepository.insert(Alarm(LocalTime.now())) else (alarmRepository.get(args.alarmId)
                    ?: alarmRepository.insert(Alarm(LocalTime.now())))

            textInput.setText(alarm.title)
            okButton.isEnabled = !textInput.text?.toString().isNullOrBlank()
            textInput.doAfterTextChanged {
                okButton.isEnabled = !it?.toString().isNullOrBlank()
            }

            view.findViewById<Button>(R.id.time_input).setOnClickListener {
                val timePicker = MaterialTimePicker.Builder()
                    //.setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(alarm.time.hour) // TODO: Create/open an alarm and show its time
                    .setMinute(alarm.time.minute)
                    .build()

                timePicker.addOnPositiveButtonClickListener {
                    alarm.time = alarm.time.withHour(timePicker.hour).withMinute(timePicker.minute)
                }

                timePicker.show(requireActivity().supportFragmentManager, "time_input_tag")
            }

            view.findViewById<Button>(R.id.time_input)
                .setText(alarm.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))

            okButton.setOnClickListener {
                lifecycleScope.launch {
                    alarm.title = textInput.text.toString()
                    alarmRepository.update(alarm)
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }
            }
        }
    }
}