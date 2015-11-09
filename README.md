# lego-wedo-java
Java library for LEGO WeDo, supports multiple hubs

# Getting Started
Here's a quick way to get started. First, clone and build the code.

    $ git clone https://github.com/kjkoster/lego-wedo-java.git
    $ cd lego-wedo-java
    $ mvn package

Then hook up your WeDo to the USB port with (at least) a motor.

    $ ./wedo list
    LEGO USB Hub V1.00
      brick A: tilt
      brick b: motor
    $ ./wedo motor 127
    $ ./wedo reset


# Reference
This code was written using these projects as a reference:
- https://github.com/itdaniher/WeDoMore
- https://github.com/Salaboy/lego-wedo4j
- https://github.com/PetrGlad/lego-wedo4j
- https://github.com/LLK/scratchx/blob/gh-pages/scratch_extensions/wedoExtension.js

