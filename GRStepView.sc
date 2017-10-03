GRStepView : GRMultiButtonView {
	var
		<>stepPressedAction,
		<>stepReleasedAction,
		<>stepValueChangedAction,
		<playhead,
		<stepViewIsCoupled, // TODO: hide under coupled
		steps
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true|
		^super.new(parent, origin, numCols, numRows, enabled, false, \toggle).initGRStepView(coupled);
	}

	initGRStepView { |argCoupled|
		stepViewIsCoupled = argCoupled;
		steps = Array.fill(this.numSteps, false);
		buttonPressedAction = { |view, x, y|
			var index = this.xyToStepIndex(x, y);
			if (stepViewIsCoupled) {
				this.setStepValueAction(index, this.stepValue(index).not);
			};
			stepPressedAction.value(this, index);
		};
		buttonReleasedAction = { |view, x, y|
			var index = this.xyToStepIndex(x, y);
			stepReleasedAction.value(this, this.xyToStepIndex(x, y));
		};
	}

	*newDetached { |numCols, numRows, enabled=true, coupled=true|
		^this.new(nil, nil, numCols, numRows, enabled, coupled)
	}

	*newDecoupled { |parent, origin, numCols, numRows, enabled=true|
		^this.new(parent, origin, numCols, numRows, enabled, false)
	}

	stepIsPressed { |index|
		var x, y;
		# x, y = this.stepIndexToXY(index);
		^this.buttonIsPressed(x, y);
	}

	stepIsReleased { |index|
		var x, y;
		# x, y = this.stepIndexToXY(index);
		^this.buttonIsReleased(x, y);
	}

	value {
		^steps.dup
	}

	value_ { |val|
		this.validateValue(val);
		this.numSteps.do { |index|
			this.setStepValue(index, val[index])
		}
	}

	valueAction_ { |val|
		var numStepValuesChanged;

		this.validateValue(val);
		numStepValuesChanged = 0;
		this.numSteps.do { |index|
			var stepValue = this.stepValue(index);
			if (stepValue != val[index]) { // TODO: this simplified version
				this.setStepValueAction(index, val[index]); // TODO: not working exactly on buttons but rather than set*-method
				numStepValuesChanged = numStepValuesChanged + 1; // TODO: should be used in GRMultiButtonView too
			}
		};
		if (numStepValuesChanged > 0) {
			this.doAction
		}
	}

	validateValue { |val|
		if (val.size != this.numSteps) {
			Error("value must be a 1-dimensional array of % values".format(this.numSteps)).throw
		}
	}

	stepValue { |index|
		^steps[index]
	}

	flashStep { |index, delay|
		var x, y;
		# x, y = this.stepIndexToXY(index);
		this.flashButton(x, y, delay);
	}

	setStepValue { |index, val|
		var buttonValue;
		steps[index] = val;
		buttonValue = this.prButtonValueByStepIndex(index);

		if (buttonValue != (val or: (playhead == index))) {
			this.prSetButtonValueByStepIndex(index, val);
		}
	}

	setStepValueAction { |index, val|
		this.setStepValue(index, val);
		stepValueChangedAction.value(this, index, val)
	}

	numSteps {
		^this.numButtons
	}

	xyToStepIndex { |x, y|
		^x + (y * this.numButtonCols)
	}

	stepIndexToXY { |index|
		^[index mod: this.numButtonCols, index div: this.numButtonCols]
	}

	refreshStep { |index|
		var x, y;
		var buttonValue;
		var stepShouldBeLit;

		# x, y = this.stepIndexToXY(index);
		buttonValue = this.buttonValue(x, y);
		stepShouldBeLit = this.stepValue(index) or: (index == playhead);

		if (buttonValue != stepShouldBeLit) {
			this.setButtonValue(x, y, stepShouldBeLit);
		};
	}

	refreshSteps { // TODO: this is just refresh or refreshView, right?
		this.numSteps.do { |index|
			this.refreshStep(index);
		};
	}

	clear {
		this.value_(Array.fill(this.numSteps) { false })
	}

	clearAction {
		this.valueAction_(Array.fill(this.numSteps) { false })
	}

	fill {
		this.value_(Array.fill(this.numSteps) { true })
	}

	fillAction {
		this.valueAction_(Array.fill(this.numSteps) { true })
	}

	playhead_ { |index|
		var previousPlayheadValue;
		previousPlayheadValue = playhead;
		playhead = index;

		if (playhead.isNil) {
			if (previousPlayheadValue.notNil) {
				this.refreshStep(previousPlayheadValue);
			}
		} {
			var stepValueAtPlayhead;
			stepValueAtPlayhead = this.stepValue(playhead);

			if (stepValueAtPlayhead) {
				this.flashStep(playhead, 100);
			} {
				this.prSetButtonValueByStepIndex(playhead, true);
			};
			if (previousPlayheadValue.notNil) {
				this.refreshStep(previousPlayheadValue);
			}
		};
	}

	prButtonValueByStepIndex { |index|
		var x, y;
		# x, y = this.stepIndexToXY(index);
		^this.buttonValue(x, y);
	}

	prSetButtonValueByStepIndex { |index, val|
		var x, y;
		# x, y = this.stepIndexToXY(index);
		this.setButtonValue(x, y, val);
	}

}
