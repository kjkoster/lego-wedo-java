# lego-wedo-java
A Java library for LEGO WeDo and for Vengit SBrick and SBrick Plus. It also comes with simple command line utilities to test controlling LEGO WeDo hubs and SBricks from the command line.

# Getting Started
Getting started with LEGO WeDo is described here on the [lego-wedo-java USB WeDo Wiki](https://github.com/kjkoster/lego-wedo-java/wiki/Controlling-USB-LEGO-WeDo-Hubs-using-lego-wedo-java).

# BLE112 Bluetooth Dongle Permissions Under Linux
By default, the BLE112 Bluetooth dongle is only readable by root and not by regular users.

To grant non-root users to access the BLE112 dongle, create a file named /etc/udev/rules.d/ble112.rules with the following incantation:

```
ATTRS{idVendor}=="2458", ATTRS{idProduct}=="0001", SUBSYSTEMS=="usb", ACTION=="add", MODE="0666"
```

Then unplug the Bluetooth dongle and plug it back in. You should now be able to address it.


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
