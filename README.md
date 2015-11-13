# lego-wedo-java
Another Java library for LEGO WeDo. This library supports multiple LEGO WeDo hubs. It also comes with a simple command line utility to control LEGO WeDo bricks from the command line.

# Getting Started
Here's a quick way to get started using the command line. First, clone and build the code.

```
$ git clone https://github.com/kjkoster/lego-wedo-java.git
$ cd lego-wedo-java
$ mvn package
```

Then hook up your WeDo to the USB port with (at least) a motor.

```
$ ./wedo list
LEGO USB Hub V1.00
  brick A: tilt
  brick b: motor
$ ./wedo motor 127
$ ./wedo reset
```

#USB Permissions Under Linux
By default, the WeDo hubs get permissions that allow only root to write to the device. Here is the typical warning that you get in this situation.

```
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
# Brick Identification
You may have noticed that in the command line example I used "./wedo reset" to switch off the motor instead of "./wedo motor 0". Go ahead, try it.

```
$ cd workspace/lego-wedo-java/
$ ./wedo list
LEGO USB Hub V1.00
  brick A: tilt
  brick b: motor
$ ./wedo motor 127
$ ./wedo list
LEGO USB Hub V1.00
  brick A: tilt
  brick b: unknown
```

Once a WeDo motor is running, the identification byte changes into a different value. That in itself is not problematic, but there are other bricks too. If you have the lights brick and switch it on, that too gets the same identification byte as a running motor. So once a motor or a light is running, there is no way to tell them apart. Using the verbose mode of the command line utility we can see this behaviour.

```
$ ./wedo -v list
Read packet from USB_0694_0003_0x7fc321c70a30.
  raw 0x40 0x69 [value A: 0x82] [id A: 0x27] [value B: 0xef] [id B: 0xef] 0x00 0x00
  [USB_0694_0003_0x7fc321c70a30 brick A: TILT id: 0x27 value: 0x82 [tilt 0x82 130 Tilt.NO_TILT]]
  [USB_0694_0003_0x7fc321c70a30 brick B: MOTOR id: 0xef value: 0xef]
LEGO USB Hub V1.00
  brick A: tilt
  brick b: motor
$ ./wedo motor 127
$ ./wedo -v list
Read packet from USB_0694_0003_0x7fa453536120.
  raw 0x40 0x65 [value A: 0x7e] [id A: 0x27] [value B: 0xfd] [id B: 0x02] 0x00 0x00
  [USB_0694_0003_0x7fa453536120 brick A: TILT id: 0x27 value: 0x7e [tilt 0x7e 126 Tilt.NO_TILT]]
  [USB_0694_0003_0x7fa453536120 brick B: UNKNOWN id: 0x02 value: 0xfd]
LEGO USB Hub V1.00
  brick A: tilt
  brick b: unknown
```

Notice how the identification byte for the motor changes from [id B: 0xef] to [id B: 0x02]. Other libraries take the approach to lump the "running" ID's with the motor ID's, but that causes shining lights to show up as a running motor, which is also confusing.

The programmatic API has a cache that remembers what brick was found on what connector. So using the WeDoBricks class solves this problem for you. At the same time it tries to deal with the dynamic nature of LEGO and USB. Both USB LEGO WeDo hubs and WeDo bricks may come and go at any time. The cache works pretty good, but you can still fool it by switching from a motor to a light while the motor was running.

To summarise: when using the **command line utility**, brick identification fails for motors and lights that are on. Use "./wedo reset" to switch them off. The **programmatic API** caches the brick identifier and does not suffer from the same problem.

# Reference
This code was written using these projects as a reference:
- https://github.com/itdaniher/WeDoMore
- https://github.com/Salaboy/lego-wedo4j
- https://github.com/PetrGlad/lego-wedo4j
- https://github.com/LLK/scratchx/blob/gh-pages/scratch_extensions/wedoExtension.js

