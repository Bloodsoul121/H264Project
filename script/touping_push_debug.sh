adb shell pm uninstall com.blood.h264.touping.push
cd ..
./gradlew installPushDebug || exit
adb shell am start com.blood.h264.touping.push/com.blood.touping.MainActivity