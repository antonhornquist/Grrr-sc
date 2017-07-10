# GrrrMonome-sc

[Monome](http://monome.org) grid support for the Grrr-sc UI toolkit

## Description

GrrrMonome-sc makes the [Grrr-sc](http://github.com/antonhornquist/Grrr-sc) grid controller UI toolkit support monome 40h, 64, 128 and 256 devices.

## Examples

### Example 1

``` supercollider
a = GRMonome64App.new;

b = GRButton.new(a, 0@0);
b.action = { |view, value| "the first button's value was changed to %".format(value).postln };

// press top-leftmost screen grid button to test the first button

c = GRButton.newMomentary(a, 1@1, 2, 2);
c.action = { |view, value| "the second button's value was changed to %".format(value).postln };

// press screen grid button anywhere at 1@1 to 2@2 to test the second button

a.free
```

## Requirements

This library requires [Grrr-sc](http://github.com/antonhornquist/Grrr-sc) and [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc).

This code has been developed and tested in SuperCollider 3.8.0.

## Installation

Make sure all dependencies are installed.

Copy the GrrrMonome-sc folder to the user-specific or system-wide extension directory. Recompile the SuperCollider class library.

The user-specific extension directory may be retrieved by evaluating Platform.userExtensionDir in SuperCollider, the system-wide by evaluating Platform.systemExtensionDir.

## License

Copyright (c) Anton Hörnquist
