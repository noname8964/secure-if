#!/system/bin/sh

setprop persist.sys.usb.config none
setprop sys.usb.config.fac none
setprop sys.usb.config none
setprop sys.usb.configfs 0

boot=$(getprop sys.usb.state)
while [ $boot = "none" ]; do
  sleep 10
  boot=$(getprop sys.usb.state)
  if [ $boot != "none" ]; then
    settings put global adb_enabled 0
    setprop persist.sys.usb.config none
    setprop sys.usb.config.fac none
    setprop sys.usb.config none
    setprop sys.usb.configfs 0
    break
    #start adbd
  fi
done