# Grrr-sc

Grid controller UI library for SuperCollider

## Description

Grrr-sc provides high level UI abstractions for grid based controllers simplifying interaction with for instance Monome devices.

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

### Example 3

``` supercollider
// post note names pressed

// TODO: no need to make newDetached and enableKeyControl

(
	var table = (), keyboard, screenGrid;
	// build a table of note names (nicked from Literals helpfile)
	value {
		var semitones = [0, 2, 4, 5, 7, 9, 11];
		var naturalNoteNames = ["C", "D", "E", "F", "G", "A", "B"];
	
		(0..9).do {|o|
			naturalNoteNames.do {|c, i|
				var n = (o + 1) * 12 + semitones[i];
				table[n] = (c ++ o).asSymbol;
				if ((c != "B") and: (c != "E")) {
					table[n+1] = (c ++ "#"  ++ o).asSymbol;
				};
			};
		};
	};

	keyboard = GRKeyboard.newDetached(7, 8)
		.notePressedAction_({ |kbd, note| "note % was pressed".format(table[note]).postln })
		.noteReleasedAction_({ |kbd, note| "note % was released".format(table[note]).postln });

	screenGrid = GRScreenGrid.newView(keyboard).enableKeyControl;
)
```

## Implementation

Code readability has been favored over optimizations.

## Classes

* GRView - Abstract superclass. Represents a 2D grid of backlit buttons.
	* GRButton - A button that may span over several rows and columns.
	* GRAbstractToggle
		* GRToggle
			* GRVToggle
			* GRHToggle
	* GRKeyboard
	* GRContainerView - Abstract class for views that may contain other views.
		* GRTopView - This is the topmost view in a view tree and typically the view to which controllers attach. The view cannot be added as a child to any other view.
		* GRMultiButtonView - A grid of buttons of the same size.

		* GRMultiToggleView - An array of vertical or horizontal toggles of the same size.
		* GRSwitcher - A container that only have one child view active at any given time. Has convenience methods for changing which child view is active.
* GRController - Abstract superclass. Represents a device that may attach to and control part of or an entire view.
	* GRScreenGrid - An on-screen controller of user definable size. Button events may be triggered with mouse and keyboard.

## Extending Grrr

### View Subclass Example

``` supercollider
MyView : GRView {
	var
		<myProperty
	;

	*new { |parent, origin, numCols=nil, numRows=nil, enabled=true, myProperty=true|
		// invoke superclass constructor
		^super.new(parent, origin, numCols, numRows, enabled).initMyView(myProperty);
	}

	initMyView { |argMyProperty|
		// save any custom properties
		myProperty = argMyProperty;

		// setup hooks
		isLitAtFunc = { |point|
			// function should return true if led at point is lit, otherwise false
		};
		viewButtonStateChangedAction = { |point, pressed|
			// handle press / release

			// if press / release results should result in a new value call
			// valueAction to set value and notify any observing objects
			this.valueAction_(newValue);

		};
	}

	// add custom class methods for instantiation here
	*newMyPropertyFalse { |parent, origin, numCols=nil, numRows=nil|
		^this.new(parent, origin, numCols, numRows, true, false);
	}

	// add custom methods here

}
```

### Controller Subclass Example

``` supercollider
MyController : GRController {
	*new { |arg1, arg2, view, origin, createTopViewIfNoneIsSupplied=true|
 		// pass numCols, numRows view and origin to superclass and basic bounds will be set up aswell as attachment to view (if view is supplied)
		^super.new(7, 8, view, origin, createTopViewIfNoneIsSupplied).initMyController(arg1, arg2);
	}

	initMyController { |arg1, arg2|

		// setup hook to trigger buttons
		// emitButtonEvent

		// refresh controller as last thing in initialize to refresh leds
		this.refresh;
	}

	// it is good practice to override newDetached with custom arguments to ensure it is 
	// possible to create an instance of the controller that is not attached to any view
	*newDetached { |arg1, arg2|
		^this.new(arg1, arg2, nil, nil, false)
	}

	handleViewLedRefreshedEvent { |point, on|
		// send update-led-message to device
	}

	handleViewButtonStateChangedEvent { |point, pressed|
		// may be used if you want to indicate button state in controller
		// example: in ScreenGrid button borders appear around ScreenGridButtons
	}

	asString {
		// optionally return a descriptive string representation
		^"My Controller connected to port % (%x%)".format(arg1, numCols, numRows)
	}
}
```

## Requirements

This code has been developed and tested in SuperCollider 3.8.0.

## Installation

Copy the Grrr-sc folder to the user-specific or system-wide extension directory. Recompile the SuperCollider class library.

The user-specific extension directory may be retrieved by evaluating Platform.userExtensionDir in SuperCollider, the system-wide by evaluating Platform.systemExtensionDir.

## License

Copyright (c) Anton Hörnquist
