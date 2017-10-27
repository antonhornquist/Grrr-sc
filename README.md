# Grrr-sc

Grid controller UI toolkit for SuperCollider.

## Description

Grrr-sc provides high level UI abstractions for grid based controllers such as [monome](http://monome.org) 40h, 64, 128 and 256 devices. Widgets, ie. buttons and toggles, are placed on controllers. Widgets can be nested in containers which allows for modes and paging. Grrr reuses principles of the standard SuperCollider GUI class library.

## Usage

Grrr can be used as a framework for building full featured apps or in live coding.

## Examples

### Hello World

``` supercollider
a=GRScreenGrid.new; // creates a virtual grid which shows up in a separate window
b=GRButton(a, 0@0); // places a 1x1 button at top left key
b.action = { |view, value| (value.if("Hello", "Goodbye") + "World").postln }; // sets an action to be triggered when the button is pressed

// pressing the top left grid button of the virtual grid will change led state and output to the Post Window
```

### An example with sound

``` supercollider
s.boot;
a=GRMonome.new; // creates a monome
b=GRButton(a, 0@0); // places a 1x1 button at top left key
b.action = { |view, value| if (value) { c = {SinOsc.ar}.play } { c.release } }; // sets an action to be triggered when the button is pressed
a.spawnGui; // spawns a virtual grid

// pressing the top left grid button of the monome or virtual grid will change led state and audition a sine oscillator
```

### A simple step sequencer

``` supercollider
s.boot;
a=GRMonome.new; // creates a monome
b=GRStepView.new(a, 0@7, 8, 1); // the step view defines when to play notes 
c=GRMultiToggleView.new(a, 0@0, 8, 7); // toggles representing note pitch
c.valuesAreInverted=true;

(
    // sequence that plays a note for steps that are lit
    fork {
        b.playhead = 0;
        inf.do {
            if (b.stepValue(b.playhead)) { (degree: c.toggleValue(b.playhead)).play };
            0.25.wait;
            b.playhead = (b.playhead + 1) % b.numCols;
        }
    }
)

a.spawnGui; // spawns a virtual grid
```

## Requirements

Grrr-sc requires the [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc) library. The library has been developed and tested in SuperCollider 3.8.0.

## Installation

Install the [SerialOSCClient-sc](http://github.com/antonhornquist/SerialOSCClient-sc) dependency.

Copy the Grrr-sc folder to the user-specific or system-wide extension directory. Recompile the SuperCollider class library.

The user-specific extension directory may be retrieved by evaluating Platform.userExtensionDir in SuperCollider, the system-wide by evaluating Platform.systemExtensionDir.

## Documentation

Reference documentation in SCDoc help format is available in the SuperCollider IDE once the Grrr library is installed.

## Tests

An automated test suite for Grrr-sc is available separately: [GrrrTests-sc](http://github.com/antonhornquist/GrrrTests-sc)

## Implementation

Code readability has been favored over optimizations.

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

It's possible to create custom widgets and add support for additional grid controllers by subclassing base classes in the Grrr library. Refer to section "Extending Grrr" in the bundled documentation for details on extending Grrr.

## License

Copyright (c) Anton Hörnquist
