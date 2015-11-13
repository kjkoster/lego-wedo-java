# lego-wedo-java
Another Java library for LEGO WeDo. This library supports multiple LEGO WeDo hubs. It also comes with a simple command line utility to control LEGO WeDo bricks from the command line.

# Getting Started
Here's a quick way to get started using the command line. First, clone and build the code.

```sh
$ git clone https://github.com/kjkoster/lego-wedo-java.git
$ cd lego-wedo-java
$ mvn package
```

Then hook up your WeDo to the USB port with (at least) a motor.

```sh
$ ./wedo list
LEGO USB Hub V1.00
  brick A: tilt
  brick b: motor
$ ./wedo motor 127
$ ./wedo reset
```

#USB Permissions Under Linux
By default, the WeDo hubs get permissions that allow only root to write to the device. Here is the typical warning that you get in this situation.

```sh
$ ./wedo list
Nov 12, 2015 9:56:01 AM org.kjkoster.wedo.usb.Usb read
WARNING: Unable to read product name from 0006:0004:00, permission issue?
No LEGO WeDo hubs found.
```

To grant non-root users to access the WeDo devices, create a file named /etc/udev/rules.d/wedo.rules with the following incantation:

```
ATTRS{idVendor}=="0694", ATTRS{idProduct}=="0003", SUBSYSTEMS=="usb", ACTION=="add", MODE="0666"
```
 
Then unplug the WeDO hubs and plug them back in. Your LEGO WeDo hubs should now be accessible.
# Reference
This code was written using these projects as a reference:
- https://github.com/itdaniher/WeDoMore
- https://github.com/Salaboy/lego-wedo4j
- https://github.com/PetrGlad/lego-wedo4j
- https://github.com/LLK/scratchx/blob/gh-pages/scratch_extensions/wedoExtension.js

