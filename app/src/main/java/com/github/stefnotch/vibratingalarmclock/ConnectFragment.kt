package com.github.stefnotch.vibratingalarmclock

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ConnectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
            if(isGranted) {
                continuePermissionsChecks()
            } else {
                // Handle the problem
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_search).setOnClickListener {
            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                continuePermissionsChecks()
            }
        }
        // view.findViewById<Button>(R.id.button_connect)
    }

    private fun continuePermissionsChecks() {
        // TODO: Check if location is enabled https://stackoverflow.com/questions/10311834/how-to-check-if-location-services-are-enabled

        if(!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            // Ideally there would a callback, but eh
            // For now, the user just has to press again
        } else {
            val textView = view?.findViewById<TextView>(R.id.connect_text_view)
            if(textView != null) {
                textView.text = "lala"
            }
        }
    }
}