# Grrr-sc

Grid controller UI widget library for SuperCollider

## Description

High level UI abstractions for grid based controllers. Simplifies interaction with for instance Monome devices.

## Examples

### Example 1

``` supercollider
a = GRScreenGrid.new;

b = GRButton.new(a, 0@0);
b.action = { |view, value| "the first button's value was changed to %".format(value).postln };

// press top-leftmost screen grid button to test the first button

c = GRButton.newMomentary(a, 1@1, 2, 2);
c.action = { |view, value| "the second button's value was changed to %".format(value).postln };

// press screen grid button anywhere at 1@1 to 2@2 to test the second button

a.view.removeAllChildren;
```


### Example 2

``` supercollider
b = GRButton.newDecoupled(a, 0@0);
b.buttonPressedAction = { "the first button was pressed!".postln };
b.buttonReleasedAction = { "the first button was released!".postln };

// press top-leftmost screen grid button to test the button

a.view.removeAllChildren;
```

## Requirements

This code was developed and have been tested in SuperCollider 3.6.6.

## Installation

Copy the Grrr-sc folder to the user-specific or system-wide extension directory. Recompile the SuperCollider class library.

The user-specific extension directory may be retrieved by evaluating Platform.userExtensionDir in SuperCollider, the system-wide by evaluating Platform.systemExtensionDir.

## License

Copyright (c) Anton Hörnquist
