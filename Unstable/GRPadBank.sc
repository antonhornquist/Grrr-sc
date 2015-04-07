GRPadBank : GRMultiButtonView {
	var
		<>padPressedAction,
		<>padReleasedAction
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true, behavior=\toggle|
		^super.new(parent, origin, numCols, numRows, enabled, coupled, behavior).initGRPadBank;
	}

	initGRPadBank {
		buttonPressedAction = buttonPressedAction.addFunc { |view, x, y|
			padPressedAction !? padPressedAction.value(view, this.posToIndex(x, y));
		};
		buttonReleasedAction = buttonReleasedAction.addFunc { |view, x, y|
			padReleasedAction !? padReleasedAction.value(view, this.posToIndex(x, y));
		};
	}

	posToIndex { |x, y|
		^((this.bottommostRow-y)*numCols)+x
	}

	indexToX { |index|
		^index % numCols
	}

	indexToY { |index|
		^this.bottommostRow-(index div: numCols)
	}

	padValue { |index|
		^this.buttonValue(this.indexToX(index), this.indexToY(index))
	}

	setPadValue { |index, value|
		this.setButtonValue(this.indexToX(index), this.indexToY(index), value)
	}

	setPadValueAction { |index, value|
		this.setButtonValueAction(this.indexToX(index), this.indexToY(index), value)
	}

	flashPad { |index, delay|
		buttons[this.indexToX(index)][this.indexToY(index)].flash(delay);
	}
}
