package com.github.stefnotch.vibratingalarmclock

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import com.github.stefnotch.vibratingalarmclock.ble.BleConnection

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        // TODO: Show this when we aren't connected and hide it again once we are actually connected
        /*
        if(!BleConnection.getInstance().isConnected()) {
            menu.findItem(R.id.action_connect)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }*/

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_connect -> {
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_FirstFragment_to_ConnectFragment)
                true
            }
            R.id.action_settings -> {
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_FirstFragment_to_SettingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}