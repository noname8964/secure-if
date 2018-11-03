#!/system/bin/sh

setprop persist.sys.usb.config none
setprop sys.usb.config.fac none
setprop sys.usb.config none
setprop sys.usb.configfs 0

boot=$(getprop sys.boot_completed)
while [ $boot = "0" ]; do
  sleep 10
  boot=$(getprop sys.boot_completed)
  if [ $boot = "1" ]; then
    setprop persist.sys.usb.config none
    setprop sys.usb.config.fac none
    setprop sys.usb.config none
    setprop sys.usb.configfs 0
    #start adbd
  fi
done