# vibrating-alarm-clock
A little Android alarm clock that talks to a vibrator

## TODO:
Persistent bluetooth connection
- Show "not connected" warning if we aren't connected
- Snooze

## Extra stuff for later
- Auto reconnect on (alarm)restart
- Better scanning interface and faster scanning
- Show that connect button if we aren't connected
- Warn on missing location (turned off) and better permissions management
- https://developer.android.com/training/notify-user/build-notification#urgent-message
- Bonding, if possible https://github.com/NordicSemiconductor/Android-BLE-Library/issues/35
- Figure out why the lipstick keeps glowing
- Tell user if connecting worked and stuff https://punchthrough.com/android-ble-guide/
- Useful reference https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/viewmodels/ScannerViewModel.java
- Have a background service so that it's not that easy to accidentally cancel the alarm https://victorleungtw.com/write-your-android-app-to-run-in-background-mode-as-a-service
- Change the alarm receiver to `<receiver android:name=".broadcastreceiver.AlarmBroadcastReceiver" android:process=":remote">`
  -  https://forums.xamarin.com/discussion/179918/alarm-manager-setrepeating-is-not-working-in-background-and-app-got-killed

## Developer information

- Uses this library https://github.com/NordicSemiconductor/Android-BLE-Library
- Very much based on https://learntodroid.com/how-to-create-a-simple-alarm-clock-app-in-android/
- Debugging: Go to `%localappdata%\Android\Sdk\platform-tools` and then [fire up `adb`](https://stackoverflow.com/questions/3643395/how-to-get-android-crash-logs)
