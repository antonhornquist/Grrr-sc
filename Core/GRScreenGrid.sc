GRScreenGrid : GRController {
	classvar
		<defaultNumCols=8,
		<defaultNumRows=8,
		<>keyControlEnabledByDefault=false,
		buttonSize=25,
		margin=10,
		keyControlAreaNumCols=8,
		keyControlAreaNumRows=4,
		keyControlAreaBorderColor,
		keymaps,
		keymapKeys,
		keymapBackspace,
		keymapLeft,
		keymapRight,
		keymapDown,
		keymapUp
	;

	var
		<readOnly,
		<keyControlEnabled,
		window,
		buttons,
		keyControlAreaOrigins,
		currentKeyControlArea=0,
		showCurrentKeyControlArea=false,
		modifierCapsIsOn=false,
		modifierShiftIsPressed=false,
		modifierCtrlIsPressed=false,
		modifierAltIsPressed=false
	;

	*new { |numCols, numRows, view, origin, createTopViewIfNoneIsSupplied=true, readOnly=false|
		^super.new(numCols ? defaultNumCols, numRows ? numCols ? defaultNumRows, view, origin, createTopViewIfNoneIsSupplied).initGRScreenGrid(readOnly);
	}

	initGRScreenGrid { |argReadOnly|
		keyControlEnabled = keyControlEnabledByDefault;
		readOnly = argReadOnly;

		keyControlAreaOrigins = Array.fill2D(
			ceil(numCols / keyControlAreaNumCols).asInteger,
			ceil(numRows / keyControlAreaNumRows).asInteger,
			{ |x, y| Point.new(x * keyControlAreaNumCols, y * keyControlAreaNumRows) }
		).flatten;

		this.prCreateWindow;
		this.prConfigureKeyboardActions;

		this.front;
		this.refresh;
	}

	*newDetached { |numCols, numRows|
		^this.new(numCols, numRows, nil, nil, false);
	}

	*newView { |view, readOnly=false|
		^GRScreenGrid.new(view.numCols, view.numRows, view, Point.new(0, 0), false, readOnly)
	}

	*initClass {
		keyControlAreaBorderColor = Color.black;
		keymaps = (
			swedish_osx: (
				keys: #[
					18, 19, 20, 21, 23, 22, 26, 28,
					12, 13, 14, 15, 17, 16, 32, 34,
					0, 1, 2, 3, 5, 4, 38, 40,
					6, 7, 8, 9, 11, 45, 46, 43
				],
				backspace: 51,
				arrowKeyLeft: 123,
				arrowKeyRight: 124,
				arrowKeyDown: 125,
				arrowKeyUp: 126
			),
			swedish_ubuntu: (
				keys: #[
					10, 11, 12, 13, 14, 15, 16, 17,
					24, 25, 26, 27, 28, 29, 30, 31,
					38, 39, 40, 41, 42, 43, 44, 45,
					52, 53, 54, 55, 56, 57, 58, 59,
				],
				backspace: 22,
				arrowKeyLeft: 113,
				arrowKeyRight: 114,
				arrowKeyDown: 116,
				arrowKeyUp: 111
			),
			swedish_windows7: (
				keys: #[
					49, 50, 51, 52, 53, 54, 55, 56,
					81, 87, 69, 82, 84, 89, 85, 73,
					65, 83, 68, 70, 71, 72, 74, 75,
					90, 88, 67, 86, 66, 78, 77, 188
				],
				backspace: 8,
				arrowKeyLeft: 37,
				arrowKeyRight: 39,
				arrowKeyDown: 40,
				arrowKeyUp: 38
			)
		);
		this.setKeymap(\swedish_windows7);
	}

	*setKeymap { |keymapName|
		keymapKeys = keymaps[keymapName][\keys];
		keymapBackspace = keymaps[keymapName][\backspace];
		keymapLeft = keymaps[keymapName][\arrowKeyLeft];
		keymapRight = keymaps[keymapName][\arrowKeyRight];
		keymapDown = keymaps[keymapName][\arrowKeyDown];
		keymapUp = keymaps[keymapName][\arrowKeyUp];
	}

	cleanup {
		this.releaseAllScreenGridButtons;
		if (window.isClosed.not) {
			{ window.close }.defer;
		};
	}

	info {
		^"%
==========
Press buttons with mouse, or enable key control with ctrl-backspace and use keyboard. Use shift to hold buttons. Use caps lock to hold and toggle buttons. If the grid is larger than key control area (%x%) it is possible to switch between areas on the ScreenGrid using the arrow buttons. Also, as long as alt is key presses are redirected to next key control area.".format(this.class, keyControlAreaNumCols, keyControlAreaNumRows)
	}

	handleViewButtonStateChangedEvent { |point, pressed|
		{ buttons[point.x][point.y].pressed = pressed }.defer;
	}

	handleViewLedRefreshedEvent { |point, on|
		{ buttons[point.x][point.y].lit = on }.defer;
	}

	front {
		window.front
	}

	alwaysOnTop {
		^window.alwaysOnTop
	}

	alwaysOnTop_ { |argAlwaysOnTop|
		window.alwaysOnTop_(argAlwaysOnTop)
	}

	toggleKeyControl {
		if (keyControlEnabled) {
			this.disableKeyControl
		} {
			this.enableKeyControl;
			this.flashKeyControlArea;
		};
	}

	enableKeyControl {
		keyControlEnabled = true;
	}

	disableKeyControl {
		this.hideKeyControlArea;
		keyControlEnabled = false;
	}

	releaseAllScreenGridButtonsWithinKeyControlAreaBoundsUnlessHoldModifier { |index|
		var keyControlAreaOrigin = keyControlAreaOrigins[index];
		if (this.holdModifier.not) {
			this.releaseAllScreenGridButtonsWithinBounds(
				argOrigin: keyControlAreaOrigin,
				argNumCols: keyControlAreaNumCols min: (numCols - keyControlAreaOrigin.x),
				argNumRows: keyControlAreaNumRows min: (numRows - keyControlAreaOrigin.y)
			);
		};
	}

	releaseAllScreenGridButtonsWithinBounds { |argOrigin, argNumCols, argNumRows|
		GRView.boundsToPoints(argOrigin, argNumCols, argNumRows).do { |point|
			this.releaseScreenGridButton(point.x, point.y)
		}
	}

	releaseAllScreenGridButtonsUnlessHoldModifier {
		if (this.holdModifier.not) {
			this.releaseAllScreenGridButtons;
		}
	}

	releaseAllScreenGridButtons {
		numCols.do { |x|
			numRows.do { |y|
				this.releaseScreenGridButton(x, y)
			}
		}
	}

	releaseScreenGridButton { |x, y|
		var button;
		button = buttons[x][y];
		if (button.isPressed) {
			button.valueAction_(false);
		}
	}

/*
	TODO: In QtGUI when a key is held down on the keyboard it repeats.
*/
	handleKeyControlEvent { |keycode, pressed|
		var keymapKeysIndex = keymapKeys.indexOf(keycode);
		if (keymapKeysIndex.notNil) {
			this.handleKeyControlKeymapEvent(keymapKeysIndex, pressed);
		} {
			if (this.isArrowKeycode(keycode)) {
				this.handleKeyControlArrowEvent(this.arrowKeycodeToDirection(keycode), pressed);
			}
		}
	}

	handleKeyControlArrowEvent { |direction, pressed|
		var currentOrigin, newOrigin;
		if ((keyControlAreaOrigins.size > 1) and: pressed) {
			currentOrigin = keyControlAreaOrigins[currentKeyControlArea];
			newOrigin = switch (direction,
				\left, keyControlAreaOrigins.detect { |p|
					(p.x == (currentOrigin.x - keyControlAreaNumCols)) and: (p.y == currentOrigin.y)
				},
				\right, keyControlAreaOrigins.detect { |p|
					(p.x == (currentOrigin.x + keyControlAreaNumCols)) and: (p.y == currentOrigin.y)
				},
				\down, keyControlAreaOrigins.detect { |p|
					(p.x == currentOrigin.x) and: (p.y == (currentOrigin.y + keyControlAreaNumRows))
				},
				\up, keyControlAreaOrigins.detect { |p|
					(p.x == currentOrigin.x) and: (p.y == (currentOrigin.y - keyControlAreaNumRows))
				}
			);

			if (newOrigin.notNil) {
				this.releaseAllScreenGridButtonsWithinKeyControlAreaBoundsUnlessHoldModifier(currentKeyControlArea);
				currentKeyControlArea = keyControlAreaOrigins.indexOfEqual(newOrigin);
			};

			this.flashKeyControlArea;
		};
	}

	handleKeyControlKeymapEvent { |keymapKeysIndex, pressed|
		var button, keyControlAreaForThisKeyEvent;
		if ((pressed.not and: this.holdModifier).not) { // Ignore key released if caps on or shift pressed
			keyControlAreaForThisKeyEvent = if (modifierAltIsPressed) { this.nextKeyControlArea } { currentKeyControlArea };
			button = this.lookupScreenGridButton(keyControlAreaForThisKeyEvent, keymapKeysIndex);
			button !? {
				if (modifierCapsIsOn) {
					button.toggleAction
				} {
					button.valueAction_(pressed)
				};
			};
		};
	}

	isArrowKeycode { |keycode|
		^this.arrowKeycodeToDirection(keycode).notNil
	}

	arrowKeycodeToDirection { |keycode|
		^switch (keycode,
			keymapLeft, \left,
			keymapRight, \right,
			keymapDown, \down,
			keymapUp, \up
		)
	}

	nextKeyControlArea {
		^(currentKeyControlArea + 1) mod: keyControlAreaOrigins.size
	}

	lookupScreenGridButton { |areaIndex, keymapKeysIndex|
		var point;
		point = keyControlAreaOrigins[areaIndex]+Point.new(keymapKeysIndex mod: keyControlAreaNumCols, keymapKeysIndex div: keyControlAreaNumCols);
		^if (this.containsPoint(point)) { buttons[point.x][point.y] }
	}

	prCreateWindow {
		var windowWidth, windowHeight;

		windowWidth = (numCols*buttonSize) + ((numCols-1)*(buttonSize*0.2)) + (margin*2);
		windowHeight = (numRows*buttonSize) + ((numRows-1)*(buttonSize*0.2)) + (margin*2);

		window = Window.new(
			"%x% %".format(numCols, numRows, this.class.asString),
			Rect(Window.screenBounds.width-windowWidth-50, 50, windowWidth, windowHeight),
			resizable: false
		);

		window.onClose = {
			if (this.isRemoved.not) { this.remove }
		};

		window.drawFunc = {
			var currentOrigin, originButton, cornerButton, originButtonBounds, cornerButtonBounds, left, top, rect;
			if (showCurrentKeyControlArea) {
				currentOrigin = keyControlAreaOrigins[currentKeyControlArea];

				originButton = Point.new(
					currentOrigin.x max: 0,
					currentOrigin.y max: 0
				);
				cornerButton = Point.new(
					( (currentOrigin.x+keyControlAreaNumCols) min: numCols) - 1,
					( (currentOrigin.y+keyControlAreaNumRows) min: numRows) - 1
				);

				originButtonBounds = buttons[originButton.x][originButton.y].bounds;
				cornerButtonBounds = buttons[cornerButton.x][cornerButton.y].bounds;

				left = originButtonBounds.left;
				top = originButtonBounds.top;

				rect = Rect.new(
					left,
					top,
					cornerButtonBounds.left - left + buttonSize,
					cornerButtonBounds.top - top + buttonSize
				).insetBy(-2);

				Pen.width = 2;
				Pen.color = keyControlAreaBorderColor;
				Pen.strokeRect(rect);
			};
		};

		buttons = Array.fill2D(numCols, numRows) { |x, y|
			var button = GRScreenGridButton( window, Rect(margin+(x*buttonSize*1.2), margin+(y*buttonSize*1.2), buttonSize, buttonSize) );
			if (readOnly.not) {
				button.action_({ |b| this.emitButtonEvent(Point.new(x, y), b.value) });
			};
			button
		};
	}

	prConfigureKeyboardActions {
		var
			topView,
			nsAlphaShiftKeyMask=65536,
			nsShiftKeyMask=131072,
			nsControlKeyMask=262144,
			nsAlternateKeyMask=524288
		;

		topView = window.view;

		topView.keyDownAction = { |view, char, modifiers, unicode, keycode|
			if (modifierCtrlIsPressed and: keycode == keymapBackspace) {
				this.toggleKeyControl;
			} {
				if (keyControlEnabled) {
					this.handleKeyControlEvent(keycode, true);
				}
			}
		};

		topView.keyUpAction = { |view, char, modifiers, unicode, keycode|
			if (keyControlEnabled) {
				this.handleKeyControlEvent(keycode, false);
			}
		};

		topView.keyModifiersChangedAction = { |view, modifiers|
			if (modifierCapsIsOn.not and: (modifiers & nsAlphaShiftKeyMask == nsAlphaShiftKeyMask)) {
				modifierCapsIsOn = true;
			};

			if (modifierCapsIsOn and: (modifiers & nsAlphaShiftKeyMask == 0)) {
				modifierCapsIsOn = false;
				this.releaseAllScreenGridButtonsUnlessHoldModifier;
			};

			if (modifierShiftIsPressed.not and: (modifiers & nsShiftKeyMask == nsShiftKeyMask)) {
				modifierShiftIsPressed = true;
			};

			if (modifierShiftIsPressed and: (modifiers & nsShiftKeyMask == 0)) {
				modifierShiftIsPressed = false;
				this.releaseAllScreenGridButtonsUnlessHoldModifier;
			};

			if (modifierCtrlIsPressed.not and: (modifiers & nsControlKeyMask == nsControlKeyMask)) {
				modifierCtrlIsPressed = true;
				if (keyControlEnabled) { this.showKeyControlArea };
			};

			if (modifierCtrlIsPressed and: (modifiers & nsControlKeyMask == 0)) {
				modifierCtrlIsPressed = false;
				if (keyControlEnabled) { this.hideKeyControlArea };
			};

			if (modifierAltIsPressed.not and: (modifiers & nsAlternateKeyMask == nsAlternateKeyMask)) {
				modifierAltIsPressed = true;
				this.releaseAllScreenGridButtonsWithinKeyControlAreaBoundsUnlessHoldModifier(currentKeyControlArea);
			};

			if (modifierAltIsPressed and: (modifiers & nsAlternateKeyMask == 0)) {
				modifierAltIsPressed = false;
				this.releaseAllScreenGridButtonsWithinKeyControlAreaBoundsUnlessHoldModifier(this.nextKeyControlArea);
			};
		};
	}

	holdModifier {
		^modifierCapsIsOn or: modifierShiftIsPressed
	}

	flashKeyControlArea {
		this.showKeyControlArea;
		modifierCtrlIsPressed.not.if {
			{ modifierCtrlIsPressed.not.if { this.hideKeyControlArea } }.defer(0.1);
		};
	}

	showKeyControlArea {
		showCurrentKeyControlArea = true;
		window.refresh;
	}

	hideKeyControlArea {
		showCurrentKeyControlArea = false;
		window.refresh;
	}
}

+ GRTopView {
	spawnGui {
		GRScreenGrid.newView(this)
	}
}

+ GRView {
	spawnGui {
		GRScreenGrid.newView(this, true) // TODO: see Things entry
	}
}
