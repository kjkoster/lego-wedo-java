# lego-wedo-java
Java library for LEGO WeDo, supports multiple hubs

# Getting Started
Here's a quick way to get started.

    $ git clone https://github.com/kjkoster/lego-wedo-java.git
    $ cd lego-wedo-java
    $ mvn package
    $ _

Then hook up your WeDo to the USB port with (at least) a motor.

    $ ./wedo list
    LEGO USB Hub V1.00
      brick A: tilt
      brick b: motor
    $ ./wedo motor 127
    $ ./wedo reset

