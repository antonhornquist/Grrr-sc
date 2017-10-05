# Grrr-sc

Grid controller UI toolkit for SuperCollider.

## Description

Grrr-sc provides high level UI abstractions for grid based controllers facilitating building complex UIs targeted to for instance [monome](http://monome.org) 40h, 64, 128 and 256 devices.

Widgets, ie. buttons and toggles, can be placed on controllers. Widgets may be placed in containers. Containers may be nested for building paged UIs and modes.

Grrr reuses principles of the standard SuperCollider GUI class library (see link::Guides/GUI-Introduction::).

## Usage

See examples below.

## Examples

### Monome Example

``` supercollider
a = GRMonome64.new;

b = GRButton.new(a, 0@0);
b.action = { |view, value| "the value of button at 0@0 was changed to %".format(value).postln };

// press top-leftmost screen grid button to test the first button

c = GRButton.newMomentary(a, 1@1, 2, 2);
c.action = { |view, value| "the second button's value was changed to %".format(value).postln };

// press screen grid button anywhere at 1@1 to 2@2 to test the second button

a.free
```

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

## Documentation

Tutorials, example apps and reference documentation in schelp format is available via the SuperCollider IDE.

## Implementation

Please do note that code readability has been favored over optimizations.

The [grrr-rb](http://github.com/antonhornquist/grrr-rb) library is a Ruby port of this library. The SuperCollider and Ruby classes are generated using the [rsclass-rb](http://github.com/antonhornquist/rsclass-rb) class generator based on meta data defined in the [grrr-meta-rb](http://github.com/antonhornquist/grrr-meta-rb) repository.

## Classes

* GRView - Abstract superclass. Represents a 2D grid of backlit buttons.
	* GRButton - A button that may span over several rows and columns.
	* GRAbstractToggle - Abstract class for toggles.
		* GRToggle - A toggle.
			* GRVToggle - Vertical toggle.
			* GRHToggle - Horizontal toggle.
	* GRKeyboard - A virtual keyboard.
	* GRContainerView - Abstract class for views that may contain other views.
		* GRTopView - This is the topmost view in a view tree and typically the view to which controllers attach. The view cannot be added as a child to any other view.
		* GRMultiButtonView - A grid of buttons of the same size.
			* GRStepView - A grid of buttons of the same size adapted for step sequencing. Buttons are indexed by step and the step currently playing can be indicated using a property of the class.
		* GRMultiToggleView - An array of vertical or horizontal toggles of the same size.
* GRController - Abstract superclass. Represents a device that may attach to and control part of or an entire view.
	* GRMonome - Abstract class for [monome](http://monome.org) controllers.
		* GRMonome64 - An 8x8 monome.
		* GRMonomeV128 - An 8x16 monome.
		* GRMonomeH128 - A 16x8 monome.
		* GRMonome256 - An 8x16 monome.
	* GRScreenGrid - An on-screen controller of user definable size. Button events may be triggered with mouse and keyboard.

## Extending Grrr

It's fairly easy to create custom widgets or provide support for additional controllers by subclassing base classes in the Grrr library. Refer to [EXTENDING.md](EXTENDING.md) for more information.

## Requirements

This code requires the [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc) library and has been developed and tested in SuperCollider 3.8.0.

## Installation

Make sure the [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc) library is properly installed.

Copy the Grrr-sc folder to the user-specific or system-wide extension directory. Recompile the SuperCollider class library.

The user-specific extension directory may be retrieved by evaluating Platform.userExtensionDir in SuperCollider, the system-wide by evaluating Platform.systemExtensionDir.

## License

Copyright (c) Anton Hörnquist
