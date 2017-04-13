# lego-wedo-java
A Java library for LEGO WeDo and for Vengit SBrick and SBrick Plus. It also comes with simple command line utilities to test controlling LEGO WeDo hubs and SBricks from the command line.

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
  brick A: tilt: forward
  brick B: motor
$ ./wedo motor 127
$ ./wedo reset
```

# WeDo USB HUB Permissions Under Linux
By default, the WeDo hubs get permissions that allow only root to write to the device. Here is the typical warning that you get in this situation.

```
$ ./wedo list
unable to read product name from 0006:0004:00, permission issue?
No LEGO WeDo hubs found.
```

To grant non-root users to access the WeDo devices, create a file named /etc/udev/rules.d/wedo.rules with the following incantation:

```
ATTRS{idVendor}=="0694", ATTRS{idProduct}=="0003", SUBSYSTEMS=="usb", ACTION=="add", MODE="0666"
```
 
Then unplug the WeDO hubs and plug them back in. Your LEGO WeDo hubs should now be accessible.

# BLE112 Bluetooth Dongle Permissions Under Linux
By default, the BLE112 Bluetooth dongle is only readable by root and not by regular users.

To grant non-root users to access the BLE112 dongle, create a file named /etc/udev/rules.d/ble112.rules with the following incantation:

```
ATTRS{idVendor}=="2458", ATTRS{idProduct}=="0001", SUBSYSTEMS=="usb", ACTION=="add", MODE="0666"
```

Then unplug the Bluetooth dongle and plug it back in. You should now be able to address it.

# Brick Addressing
Addressing bricks is tricky because neither USB nor LEGO WeDo hubs offer any way to uniquely identify an individual brick. WeDo sets come with a single hub and it is pretty obvious that they were designed to have only that one, single hub. This is fine for builds that use only a single hub, of course, but one of the design goals of the lego-wedo-java library is to support multiple hubs.

The lego-wedo-java library passes this problem on to your program by offering only abstract addressing of bricks. So there is no way to specify "the motor on connector A on hub 2". You can only specify "all motors on connectors A of all hubs". lego-wedo-java always iterates over all hubs, trying to find matching bricks on each hub.

Likewise for sensors. When asking for the distance measurement, the API returns a list with a sample of all distance sensors on all hubs. This ensures that your sensors will be read even when a hub us unplugged and plugged back into your computer.

This means that for those who want to build a model with more than two sensors and actuators, some creativity is expected to ensure easy addressing.

Each hub has two connectors, named A and B. Below is a crude ASCII-art picture showing which connector is which.

```
     |
(---------)
| 0 0 0 0 |
| 0 0 0 0 |
+---------+
| 0 0 0 0 |
| (=) (=) |
+---------+
   A   B
```

As an aside: The standard software that LEGO supplies for WeDo actually supports up to three hubs. Just hold down the [SHIFT] key when clicking on a motor or lights component. You'll see dots appear over the block that indicate what item is being addressed. Scratch is limited to a single hub, usually the one that was plugged into your computer last.

# Brick Identification
You may have noticed that in the command line example I used "./wedo reset" to switch off the motor instead of "./wedo motor 0". Go ahead, try it.

```
$ ./wedo list
LEGO USB Hub V1.00
  brick A: tilt: forward
  brick B: motor
$ ./wedo motor 127
$ ./wedo list
LEGO USB Hub V1.00
  brick A: tilt: forward
  brick B: unknown
```

Once a WeDo motor is running, the identification byte changes into a different value. That in itself is not problematic, but there are other bricks too. If you have the lights brick and switch it on, it gets the exact same identification byte as a running motor. So once a motor or a light is running, there is no way to tell them apart. Using the verbose mode of the command line utility we can see this behaviour.

```
$ ./wedo -v list
loading native HID library
  USB read USB_0694_0003_0x7ff05bf13280: 0x40 0x69 [value A: 0xb4] [id A: 0x27] [value B: 0xef] [id B: 0xef] 0x00 0x00
read [USB_0694_0003_0x7ff05bf13280 brick A: TILT id: 0x27 value: 0xb4 [tilt 0xb4 FORWARD]]
read [USB_0694_0003_0x7ff05bf13280 brick B: MOTOR id: 0xef value: 0xef]
LEGO USB Hub V1.00
  brick A: tilt: forward
  brick B: motor
$ ./wedo motor 127
$ ./wedo -v list
loading native HID library
  USB read USB_0694_0003_0x7facf061d610: 0x40 0x66 [value A: 0xb5] [id A: 0x27] [value B: 0xfe] [id B: 0x02] 0x00 0x00
read [USB_0694_0003_0x7facf061d610 brick A: TILT id: 0x27 value: 0xb5 [tilt 0xb5 FORWARD]]
read [USB_0694_0003_0x7facf061d610 brick B: UNKNOWN id: 0x02 value: 0xfe]
LEGO USB Hub V1.00
  brick A: tilt: forward
  brick B: unknown
```

Notice how the identification byte for the motor changes from [id B: 0xef] to [id B: 0x02]. Other libraries take the approach to lump the "running" ID's with the motor ID's, but that causes shining lights to show up as a running motor, which is also confusing.

The programmatic API has a cache that remembers what brick was found on what connector. So using the WeDoBricks class solves this problem for you. At the same time it tries to deal with the dynamic nature of LEGO and USB. Both USB LEGO WeDo hubs and WeDo bricks may come and go at any time. The cache works pretty good, but you can still fool it by switching from a motor to a light while the motor was running.

To summarise: when using the **command line utility**, brick identification fails for motors and lights that are on. Use "./wedo reset" to switch them off. The **programmatic API** caches the brick identifier and does not suffer from the same problem.

# Playing Sound Clips
Playing sound clips are easy from the original LEGO WeDo software and I have to say it adds a lot to the build, silly as it may seem. Adding sound clips proved both more hard and more easy than I thought.

It is hard to add sound clips because of the rights to the various clip files. Obviously I cannot publish the original LEGO WeDo sounds. Making my own sounds is also not very successful (ahem).

On the other hand, playing a sound clip in Java is really easy.

```java
final File file = new File(fileName);
if (file.exists()) {
    final AudioInputStream sound = AudioSystem.getAudioInputStream(file);
    final Clip clip = AudioSystem.getClip();
    clip.open(sound);
    
    clip.start();
    clip.drain();
    clip.stop();
} else {
    throw new IOException(file + " does not exist");
}
```

That is all. I thought to add it to the WeDoBricks class, but that would just hide the controls for you. So here it is. I'm not adding sound support to this WeDO library, simply because Java already makes that super easy.

# Reference
This code was written using these projects as a reference:
- https://github.com/itdaniher/WeDoMore
- https://github.com/Salaboy/lego-wedo4j
- https://github.com/PetrGlad/lego-wedo4j
- https://github.com/LLK/scratchx/blob/gh-pages/scratch_extensions/wedoExtension.js
- https://gist.github.com/blindman2k/11353842

https://social.sbrick.com/wiki/view/pageId/11/slug/the-sbrick-ble-protocol
https://social.sbrick.com/wiki/view/pageId/20/slug/linux-client-scripts
