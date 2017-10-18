# Grrr-sc

Grid controller UI toolkit for SuperCollider.

## Description

Grrr-sc provides high level UI abstractions for grid based controllers such as [monome](http://monome.org) 40h, 64, 128 and 256 devices.

Using Grrr grid controller UIs can be built in the way SuperCollider GUIs are built.

Widgets, ie. buttons and toggles, can be placed on controllers. Widgets may be placed in containers. Containers may be nested for building paged UIs and modes.

Instead of implementing a grid controller UI features by implementing "when a button in column 1 is pressed set it lit and all other buttons in column 1 to unlit" one can implement "column 1 should be a toggle".

Widgets can be nested in containers which allows for widgets to be handled in different pages.

Grrr reuses principles of the standard SuperCollider GUI class library (see link::Guides/GUI-Introduction::).

## Usage

Grrr can be used as a framework for building [complete apps](http://github.com/antonhornquist/dw-sc) or in live coding.

## Examples

### A Simple Step Sequencer

``` supercollider
a = GRMonome64.new;

b = GRStepView.new(a, 0@0);
b.action = { |view, value| "the value of button at 0@0 was changed to %".format(value).postln };

// press top-leftmost screen grid button to test the first button

c = GRButton.newMomentary(a, 1@1, 2, 2);
c.action = { |view, value| "the second button's value was changed to %".format(value).postln };

// press screen grid button anywhere at 1@1 to 2@2 to test the second button

a.free
```

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

## Requirements

Grrr-sc requires the [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc) library. The library has been developed and tested in SuperCollider 3.8.0.

## Installation

Install the [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc) dependency.

Copy the Grrr-sc folder to the user-specific or system-wide extension directory. Recompile the SuperCollider class library.

The user-specific extension directory may be retrieved by evaluating Platform.userExtensionDir in SuperCollider, the system-wide by evaluating Platform.systemExtensionDir.

## Documentation

Tutorials, example apps and reference documentation in SCDoc help format is installed and available in the SuperCollider IDE once the library is installed.

## Implementation

Code readability has been favored over optimizations.

An extensive automated test suite for Grrr-sc is available separately [GrrrTests-sc](http://github.com/antonhornquist/GrrrTests-sc).

The [grrr-rb](http://github.com/antonhornquist/grrr-rb) library is a Ruby port of this library. The SuperCollider and Ruby classes are generated using the [rsclass-rb](http://github.com/antonhornquist/rsclass-rb) class generator based on meta data defined in the [grrr-meta-rb](http://github.com/antonhornquist/grrr-meta-rb) repository.

## Classes

* GRView - Abstract superclass. Represents a 2D grid of backlit buttons.
	* GRButton - A button that may span over several rows and columns.
	* GRAbstractToggle - Abstract class for toggles.
		* GRToggle - A toggle.
			* GRVToggle - Vertical oriented toggle.
			* GRHToggle - Horizontal oriented toggle.
	* GRKeyboard - A virtual keyboard.
	* GRContainerView - Abstract class for views that may contain other views.
		* GRTopView - This is the topmost view in a view tree and typically the view to which controllers attach. The view cannot be added as a child to any other view.
		* GRMultiButtonView - A grid of buttons of the same size.
			* GRStepView - A grid of buttons of the same size referred to by index. One of the steps may be indicated as the playhead position. Suitable for step sequencing.
		* GRMultiToggleView - An array of vertical or horizontal toggles of the same size.
* GRController - Abstract superclass. Represents a grid based controller device that may attach to and control part of or an entire view.
	* GRMonome - Generic [Monome](http://monome.org) controller.
		* GRMonome64 - 8x8 monome.
		* GRMonomeV128 - 8x16 monome.
		* GRMonomeH128 - 16x8 monome.
		* GRMonome256 - 8x16 monome.
	* GRScreenGrid - An on-screen controller of user definable size. Button events may be triggered with mouse and keyboard.

## Extending Grrr

It's possible to create custom widgets and add support for additional grid controllers by subclassing base classes in the Grrr library. Refer to section "Extending Grrr" in documentation for details on extending Grrr.

## License

Copyright (c) Anton Hörnquist
