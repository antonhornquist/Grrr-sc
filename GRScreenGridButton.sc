/*

	QT implementation using QUserView

*/

GRScreenGridButton : QUserView {
	var
		<value=false,
		lit=false,
		pressed=false,
		<>pressedColor,
		<>releasedColor,
		<>litColor,
		<>unlitColor,
		nsShiftKeyMask=131072,
		nsAlphaShiftKeyMask=65536
	;

	*new { |argParent, argBounds|
		^super.new(argParent, argBounds).initScreenGridButton;
	}

	initScreenGridButton {
		// set defaults of your instance variables
		pressedColor = Color.black;
		releasedColor = Color.clear;
		litColor = Color.new255(255, 140, 0);
		unlitColor = Color.grey;
		this.canFocus_(false);
		
		// set the draw function of the UserView
		this.drawFunc = { this.draw }; 
	}

	draw {
		Pen.fillColor = if (lit) { litColor } { unlitColor };
		Pen.strokeColor = if (pressed) { pressedColor } { releasedColor };
		Pen.addRect(Rect(0, 0, this.bounds.width, this.bounds.height));
		Pen.width = this.bounds.width / 4;
		Pen.fillStroke;
	}

	isLit {
		^lit
	}

	lit_ { |bool|
		lit=bool;
		this.refresh;
	}

	isPressed {
		^pressed
	}

	pressed_ { |bool|
		pressed = bool;
		this.refresh;
	}

	valueAction_ { |bool|
		if (value != bool) {
			this.value=bool;
			this.doAction;
		}
	}

	value_ { |bool|
		value=bool;
	}

	toggleAction {
		this.valueAction_(value.not);
	}

	mouseDown { |x, y, modifiers, buttonNumber, clickCount|
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount); 
		if (this.capsModifier(modifiers)) {
			this.toggleAction
		} {
			this.valueAction_(true)
		}
	}

	mouseUp { |x, y, modifiers, buttonNumber, clickCount|
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount); 
		if (this.checkHoldModifier(modifiers).not) { this.valueAction_(false) }
	}

	checkHoldModifier { |modifiers|
		^this.shiftModifier(modifiers) or: this.capsModifier(modifiers)
	}

	shiftModifier { |modifiers|
		^modifiers & nsShiftKeyMask == nsShiftKeyMask
	}

	capsModifier { |modifiers|
		^modifiers & nsAlphaShiftKeyMask == nsAlphaShiftKeyMask
	}
}

/*

	Old (SuperCollider pre-3.6) Cocoa implementation using SCUserView

GRScreenGridButton : SCUserView {
	var
		<value=false,
		lit=false,
		pressed=false,
		<>pressedColor,
		<>releasedColor,
		<>litColor,
		<>unlitColor,
		nsShiftKeyMask=131072,
		nsAlphaShiftKeyMask=65536
	;

	*viewClass {
		^SCUserView // this ensures that SCUserView's primitive is called 
	}

	init { |argParent, argBounds|
		super.init(argParent, argBounds);  
				
		// set defaults of your instance variables
		pressedColor = Color.black;
		releasedColor = Color.clear;
		litColor = Color.new255(255, 140, 0);
		unlitColor = Color.grey;
		this.canFocus_(false);
		
		// set the draw function of the UserView
		this.drawFunc = { this.draw }; 
	}

	draw {
		Pen.fillColor = if (lit) { litColor } { unlitColor };
		Pen.strokeColor = if (pressed) { pressedColor } { releasedColor };
		Pen.addRect(Rect(0, 0, this.bounds.width, this.bounds.height));
		Pen.width = this.bounds.width / 4;
		Pen.fillStroke;
	}

	isLit {
		^lit
	}

	lit_ { |bool|
		lit=bool;
		this.refresh;
	}

	isPressed {
		^pressed
	}

	pressed_ { |bool|
		pressed = bool;
		this.refresh;
	}

	valueAction_ { |bool|
		if (value != bool) {
			this.value=bool;
			this.doAction;
		}
	}

	value_ { |bool|
		value=bool;
	}

	toggleAction {
		this.valueAction_(value.not);
	}

	mouseDown { |x, y, modifiers, buttonNumber, clickCount|
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount); 
		if (this.capsModifier(modifiers)) {
			this.toggleAction
		} {
			this.valueAction_(true)
		}
	}

	mouseUp { |x, y, modifiers, buttonNumber, clickCount|
		mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount); 
		if (this.checkHoldModifier(modifiers).not) { this.valueAction_(false) }
	}

	checkHoldModifier { |modifiers|
		^this.shiftModifier(modifiers) or: this.capsModifier(modifiers)
	}

	shiftModifier { |modifiers|
		^modifiers & nsShiftKeyMask == nsShiftKeyMask
	}

	capsModifier { |modifiers|
		^modifiers & nsAlphaShiftKeyMask == nsAlphaShiftKeyMask
	}
}
*/
