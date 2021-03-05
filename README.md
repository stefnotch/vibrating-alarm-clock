# vibrating-alarm-clock
A little Android alarm clock that talks to a vibrator

## TODO:
https://developer.android.com/training/scheduling/alarms#boot
https://stackoverflow.com/a/33110418
https://victorleungtw.com/write-your-android-app-to-run-in-background-mode-as-a-service
https://github.com/learntodroid/SimpleAlarmClock/blob/8b5901e7d37182479a3accdbfd6997b0f7c8f39c/app/src/main/AndroidManifest.xml

Alarm Ring foreground service (with snooze?)
Alarm Reschedule service (like, on startup and also after an alarm got stopped)
Connect to device


## Extra stuff for later
- TODO: Hold onto the menu and show that connect button if we aren't connected
- Snooze
- Application start: Check if all current alarms are scheduled properly, if not, reschedule them

## Developer information

Uses this libray https://github.com/NordicSemiconductor/Android-BLE-Library
Very much based on https://learntodroid.com/how-to-create-a-simple-alarm-clock-app-in-android/