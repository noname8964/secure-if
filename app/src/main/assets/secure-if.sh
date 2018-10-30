#!/system/bin/sh

setprop persist.sys.usb.config none
setprop sys.usb.config none
setprop sys.usb.configfs 0
start adbd