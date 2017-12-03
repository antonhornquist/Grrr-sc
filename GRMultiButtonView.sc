GRMultiButtonView : GRView {
	var
		<>buttonPressedAction,
		<>buttonReleasedAction,
		<>buttonValueChangedAction,
		<numButtonCols,
		<numButtonRows,
		<behavior,
		coupled,
		buttons,
		containerView
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true, behavior=\toggle|
		^super.new(nil, nil, numCols, numRows, enabled).initGRMultiButtonView(parent, origin, coupled, behavior);
	}

	initGRMultiButtonView { |argParent, argOrigin, argCoupled, argBehavior|
		coupled = argCoupled;
		behavior = argBehavior;
		numButtonCols = numCols;
		numButtonRows = numRows;

		containerView = GRContainerView.newDetached(numCols, numRows);
		containerView.addAction({ |originatingButton, point, on|
			if (this.hasViewLedRefreshedAction) {
				viewLedRefreshedAction.value(this, point, on);
			};
		}, \viewLedRefreshedAction);
		this.addAction({ |point, pressed|
			containerView.handleViewButtonEvent(this, point, pressed);
		}, \viewButtonStateChangedAction);

		this.prReconstructChildren;

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols, numRows, enabled=true, coupled=true, behavior=\toggle|
		^this.new(nil, nil, numCols, numRows, enabled, coupled, behavior)
	}

	*newDecoupled { |parent, origin, numCols, numRows, enabled=true, behavior=\toggle|
		^this.new(parent, origin, numCols, numRows, enabled, false, behavior)
	}

	isCoupled {
		^coupled
	}

	coupled_ { |argCoupled|
		this.prButtonsDo { |button, x, y| button.coupled = argCoupled };
		coupled = argCoupled;
	}

	behavior_ { |argBehavior|
		this.prButtonsDo { |button, x, y| button.behavior = argBehavior };
		behavior = argBehavior;
	}

	buttonIsPressed { |x, y|
		^buttons[x][y].isPressed
	}

	buttonIsReleased { |x, y|
		^buttons[x][y].isReleased
	}

	clear {
		this.value_(Array.fill2D(numButtonCols, numButtonRows) { false })
	}

	clearAction {
		this.valueAction_(Array.fill2D(numButtonCols, numButtonRows) { false })
	}

	fill {
		this.value_(Array.fill2D(numButtonCols, numButtonRows) { true })
	}

	fillAction {
		this.valueAction_(Array.fill2D(numButtonCols, numButtonRows) { true })
	}

	value {
		^buttons.collect { |row| row.collect { |button| button.value } }
	}

	value_ { |val|
		this.validateValue(val);
		this.prButtonsDo { |button, x, y|
			button.value = val[x][y]
		};
	}

	valueAction_ { |val|
		var numButtonValuesChanged;

		this.validateValue(val);
		numButtonValuesChanged = 0;
		this.prButtonsDo { |button, x, y|
			var newButtonValue = val[x][y];
			if (this.buttonValue(x, y) != newButtonValue) {
				this.setButtonValue(x, y, newButtonValue);
				buttonValueChangedAction !? buttonValueChangedAction.value(this, x, y, newButtonValue);
				numButtonValuesChanged = numButtonValuesChanged + 1;
			}
		};
		if (numButtonValuesChanged > 0) {
			this.doAction
		}
	}

	validateValue { |val|
		if ( ((val.size == numButtonCols) and: (val.every { |row| row.size == numButtonRows})).not ) {
			Error("value must be a 2-dimensional array of %x% values".format(numButtonCols, numButtonRows)).throw
		};
		this.prButtonsDo { |button, x, y|
			button.validateValue(val[x][y]);
		};
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

	setButtonValue { |x, y, val|
		buttons[x][y].value = val
	}

	setButtonValueAction { |x, y, val|
		buttons[x][y].valueAction = val;
	}

	numButtonCols_ { |argNumButtonCols|
		this.prSetNumButtonCols(argNumButtonCols);
		this.prReconstructChildren;
	}

	numButtonRows_ { |argNumButtonRows|
		this.prSetNumButtonRows(argNumButtonRows);
		this.prReconstructChildren;
	}

	buttonArraySize {
		^Point.new(numButtonCols, numButtonRows);
	}

	buttonArraySize_ { |argButtonArraySize|
		this.prSetNumButtonCols(argButtonArraySize.x);
		this.prSetNumButtonRows(argButtonArraySize.y);
		this.prReconstructChildren;
	}

	numButtons {
		^numButtonCols * numButtonRows
	}

	buttonWidth {
		^numCols / numButtonCols
	}

	buttonHeight {
		^numRows / numButtonRows
	}

	isLitAt { |point|
		this.validateContainsPoint(point);
		^containerView.isLitAt(point);
	}

	flashButton { |x, y, delay|
		buttons[x][y].flash(delay)
	}

	flashView { |delay|
		this.prButtonsDo { |button, x, y|
			button.flash(delay);
		}
	}

	flashPoints { |points, delay|
		Error("not implemented for GRMultiButtonView").throw
	}

	prButtonsDo { |func|
		numButtonCols.do { |x|
			numButtonRows.do { |y|
				func.value(buttons[x][y], x, y);
			}
		}
	}

	prSetNumButtonCols { |argNumButtonCols|
		if (numCols % argNumButtonCols == 0) {
			numButtonCols = argNumButtonCols;
		} {
			Error("% height (%) must be divisable by number of button columns (%)".format(this.class, numCols, argNumButtonCols)).throw
		};
	}

	prSetNumButtonRows { |argNumButtonRows|
		if (numRows % argNumButtonRows == 0) {
			numButtonRows = argNumButtonRows;
		} {
			Error("% height (%) must be divisable by number of button rows (%)".format(this.class, numRows, argNumButtonRows)).throw
		};
	}

	prReconstructChildren {
		this.releaseAll;

		this.prDoThenRefreshChangedLeds {
			containerView.prRemoveAllChildren(true);
			buttons = Array.fill2D(numButtonCols, numButtonRows) { |x, y|
				var button = GRButton.newDetached(this.buttonWidth, this.buttonHeight);
				button.coupled = coupled;
				button.behavior = behavior;
				this.prAddActions(button, x, y);
				containerView.prAddChildNoFlash(button, Point.new(x*this.buttonWidth, y*this.buttonHeight));
				button;
			};
		};
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
