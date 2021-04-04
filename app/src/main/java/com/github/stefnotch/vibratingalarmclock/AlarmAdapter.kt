package com.github.stefnotch.vibratingalarmclock

import android.content.Context
import android.os.ParcelUuid
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class AlarmAdapter(private var alarms: List<Alarm>, private val navController: NavController, private val lifecycleScope: LifecycleCoroutineScope, private val context: Context) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {

    private val checkedAlarmPositions: HashSet<Int> = HashSet<Int>()
    private var supportActionMode: ActionMode? = null

    inner class ActionModeCallback: ActionMode.Callback {
        var resetSelection = true
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId) {
                R.id.action_delete -> {
                    lifecycleScope.launch  {
                        val alarmRepository = AlarmRepository(context)
                        checkedAlarmPositions.forEach { position ->
                            alarms[position].cancelAlarm(context)
                            alarmRepository.delete(alarms[position])
                        }
                        alarms = alarmRepository.getAllNonSnoozed()
                        notifyDataSetChanged()
                        supportActionMode?.finish()
                    }
                    return true
                }
            }
            return false
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.menu_alarms_action_mode, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            menu?.findItem(R.id.action_delete)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            if(resetSelection) {
                checkedAlarmPositions.clear()
                notifyDataSetChanged()
            }
            resetSelection = true
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById<MaterialCardView>(R.id.alarm_card)
        val nameElement: TextView = itemView.findViewById<TextView>(R.id.alarm_name)
        val timeElement: TextView = itemView.findViewById<TextView>(R.id.alarm_time)
        val daysElement: TextView = itemView.findViewById<TextView>(R.id.alarm_days)
        val isRunningElement: SwitchMaterial = itemView.findViewById<SwitchMaterial>(R.id.alarm_enable)
        init {
            card.setOnLongClickListener {
                card.isChecked = !card.isChecked
                true
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.alarm_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val debugId = alarms[position].id
        viewHolder.nameElement.text = alarms[position].title + "($debugId)"
        viewHolder.timeElement.text = alarms[position].getFormattedTime(context)
        viewHolder.daysElement.text = alarms[position].getDaysText()
        viewHolder.isRunningElement.isChecked = alarms[position].isRunning
        viewHolder.card.setOnClickListener {
            navController.navigate(FirstFragmentDirections.actionFirstFragmentToSecondFragment(ParcelUuid(alarms[position].id)))
        }
        viewHolder.isRunningElement.setOnClickListener {
            if(viewHolder.isRunningElement.isChecked) {
                alarms[position].scheduleAlarm(context)
            } else {
                alarms[position].cancelAlarm(context)
            }
            lifecycleScope.launch  {
                val alarmRepository = AlarmRepository(context)
                alarmRepository.update(alarms[position])
            }
        }

        viewHolder.card.isChecked = checkedAlarmPositions.contains(position)
        viewHolder.card.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                checkedAlarmPositions.add(position)
            } else {
                checkedAlarmPositions.remove(position)
            }

            val appCompatActivity = (viewHolder.card.context as AppCompatActivity)
            if(checkedAlarmPositions.isNotEmpty()) {
                if(supportActionMode == null) {
                    supportActionMode = appCompatActivity?.startSupportActionMode(ActionModeCallback())
                }
            } else {
                supportActionMode?.finish()
                supportActionMode = null
            }
        }
    }

    override fun getItemCount(): Int = alarms.size
}