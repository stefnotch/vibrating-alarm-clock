# vibrating-alarm-clock
A little Android alarm clock that talks to a vibrator

## TODO:
https://victorleungtw.com/write-your-android-app-to-run-in-background-mode-as-a-service
https://github.com/NordicSemiconductor/Android-nRF-Blinky/blob/master/app/src/main/java/no/nordicsemi/android/blinky/viewmodels/ScannerViewModel.java
https://punchthrough.com/android-ble-guide/

Persistent bluetooth connection

## Extra stuff for later
- Auto reconnect on (alarm)restart
- Show scan results
- Show that connect button if we aren't connected
- Warn on missing location (turned off) and better permissions management
- Snooze
- https://developer.android.com/training/notify-user/build-notification#urgent-message
- Bonding, if possible https://github.com/NordicSemiconductor/Android-BLE-Library/issues/35
- Proper name

## Developer information

- Uses this library https://github.com/NordicSemiconductor/Android-BLE-Library
- Very much based on https://learntodroid.com/how-to-create-a-simple-alarm-clock-app-in-android/
- Debugging: Go to `%localappdata%\Android\Sdk\platform-tools` and then [fire up `adb`](https://stackoverflow.com/questions/3643395/how-to-get-android-crash-logs)
