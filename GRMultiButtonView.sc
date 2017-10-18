GRMultiButtonView : GRContainerView {
	var
		<>buttonPressedAction,
		<>buttonReleasedAction,
		<>buttonValueChangedAction,
		<buttonArraySize,
		<behavior,
		coupled,
		buttons
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true, behavior=\toggle|
		^super.new(nil, nil, numCols, numRows, enabled, true).initGRMultiButtonView(parent, origin, coupled, behavior);
	}

	initGRMultiButtonView { |argParent, argOrigin, argCoupled, argBehavior|
		coupled = argCoupled;
		behavior = argBehavior;
		buttonArraySize = [numCols, numRows];

		actsAsView = true;

		this.prReconstructChildren;

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols, numRows, enabled=true, coupled=true, behavior=\toggle|
		^this.new(nil, nil, numCols, numRows, enabled, coupled, behavior)
	}

	*newDisabled { |parent, origin, numCols, numRows, coupled=true, behavior=\toggle|
		^this.new(parent, origin, numCols, numRows, false, coupled, behavior)
	}

	*newDecoupled { |parent, origin, numCols, numRows, enabled=true, behavior=\toggle|
		^this.new(parent, origin, numCols, numRows, enabled, false, behavior)
	}

	isCoupled {
		^coupled
	}

	coupled_ { |argCoupled|
		children.do { |child| child.coupled = argCoupled };
		coupled = argCoupled;
	}

	behavior_ { |argBehavior|
		children.do { |child| child.behavior = argBehavior };
		behavior = argBehavior;
	}

	buttonIsPressed { |x, y|
		^buttons[x][y].isPressed
	}

	buttonIsReleased { |x, y|
		^buttons[x][y].isReleased
	}

	clear {
		this.value_(Array.fill2D(this.numButtonCols, this.numButtonRows) { false })
	}

	clearAction {
		this.valueAction_(Array.fill2D(this.numButtonCols, this.numButtonRows) { false })
	}

	fill {
		this.value_(Array.fill2D(this.numButtonCols, this.numButtonRows) { true })
	}

	fillAction {
		this.valueAction_(Array.fill2D(this.numButtonCols, this.numButtonRows) { true })
	}

	value {
		^buttons.collect { |row| row.collect { |button| button.value } }
	}

	value_ { |val|
		this.validateValue(val);
		this.numButtonCols.do { |x|
			this.numButtonRows.do { |y|
				buttons[x][y].value = val[x][y]
			}
		}
	}

	valueAction_ { |val|
		var numButtonValuesChanged;

		this.validateValue(val);
		numButtonValuesChanged = 0;
		this.numButtonCols.do { |x|
			this.numButtonRows.do { |y|
				var button = buttons[x][y];
				if (button.value != val[x][y]) {
					button.value = val[x][y];
					buttonValueChangedAction !? buttonValueChangedAction.value(this, x, y, button.value);
					numButtonValuesChanged = numButtonValuesChanged + 1;
				}
			}
		};
		if (numButtonValuesChanged > 0) {
			this.doAction
		}
	}

	validateValue { |val|
		if ( ((val.size == this.numButtonCols) and: (val.every { |row| row.size == this.numButtonRows})).not ) {
			Error("value must be a 2-dimensional array of %x% values".format(this.numButtonCols, this.numButtonRows)).throw
		}
	}

	buttonsPressed {
		^buttons.collect { |row, x|
			row.collect { |button, y|
				[button, x@y]
			}
		}.flatten.select { |buttonAndPos|
			buttonAndPos[0].isPressed
		}.collect { |buttonAndPos|
			buttonAndPos[1]
		}
	}

	buttonValue { |x, y|
		^buttons[x][y].value
	}

	flashButton { |x, y, delay|
		buttons[x][y].flash(delay)
	}

	setButtonValue { |x, y, val|
		buttons[x][y].value = val
	}

	setButtonValueAction { |x, y, val|
		buttons[x][y].valueAction = val;
	}

	buttonArraySize_ { |argButtonArraySize|
		this.validateButtonArraySize(argButtonArraySize);
		buttonArraySize = argButtonArraySize;
		this.prReconstructChildren;
	}

	validateButtonArraySize { |argButtonArraySize|
		var newNumButtonCols, newNumButtonRows;
		newNumButtonCols = argButtonArraySize[0];
		newNumButtonRows = argButtonArraySize[1];
		if (numCols % newNumButtonCols != 0) {
			Error("% width (%) must be divisable by number of button columns (%)".format(this.class, numCols, newNumButtonCols)).throw
		};
		if (numRows % newNumButtonRows != 0) {
			Error("% height (%) must be divisable by number of button rows (%)".format(this.class, numRows, newNumButtonRows)).throw
		}
	}

	numButtonCols {
		^buttonArraySize[0]
	}

	numButtonRows {
		^buttonArraySize[1]
	}

	numButtons {
		^this.numButtonCols * this.numButtonRows
	}

	buttonWidth {
		^numCols / this.numButtonCols
	}

	buttonHeight {
		^numRows / this.numButtonRows
	}

	flashView { |delay|
		this.numButtonCols.do { |x|
			this.numButtonRows.do { |y|
				buttons[x][y].flash(delay);
			}
		}
	}

	flashPoints { |points, delay|
		Error("not implemented for GRMultiButtonView").throw
	}

	flashPoint { |point, delay|
		Error("not implemented for GRMultiButtonView").throw
	}

	prReconstructChildren {
		this.releaseAll;
		this.prRemoveAllChildren(true);

		buttons = Array.fill2D(this.numButtonCols, this.numButtonRows) { |x, y|
			var button = GRButton.newDetached(this.buttonWidth, this.buttonHeight);
			button.coupled = coupled;
			this.prAddActions(button, x, y);
			this.prAddChildNoFlash(button, Point.new(x*this.buttonWidth, y*this.buttonHeight));
			button;
		}
	}

	prAddActions { |button, x, y|
		button.buttonPressedAction = { |button|
			buttonPressedAction !? buttonPressedAction.value(this, x, y)
		};
		button.buttonReleasedAction = { |button|
			buttonReleasedAction !? buttonReleasedAction.value(this, x, y)
		};
		button.action = { |button, value|
			buttonValueChangedAction !? buttonValueChangedAction.value(this, x, y, value);
			this.doAction;
		};
	}
}
