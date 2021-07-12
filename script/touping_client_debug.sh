adb shell pm uninstall com.blood.h264.touping.client
cd ..
./gradlew installClientDebug || exit
adb shell am start com.blood.h264.touping.client/com.blood.touping.MainActivity