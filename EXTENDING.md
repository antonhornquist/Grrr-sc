## Extending Grrr

TODO

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

