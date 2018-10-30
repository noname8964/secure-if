#!/system/bin/sh

echo 0 > /sys/devices/virtual/android_usb/android0/enable
chmod 444 /sys/devices/virtual/android_usb/android0/enable