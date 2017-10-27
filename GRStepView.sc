GRStepView : GRMultiButtonView {
	var
		<>stepPressedAction,
		<>stepReleasedAction,
		<>stepValueChangedAction,
		<playhead,
		<stepViewIsCoupled,
		steps
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true|
		^super.new(parent, origin, numCols, numRows, enabled, false, \toggle).initGRStepView(coupled);
	}

	initGRStepView { |argCoupled|
		stepViewIsCoupled = argCoupled;
		steps = Array.fill(this.numSteps, false);
		buttonPressedAction = { |view, x, y|
			var index = this.prXyToIndex(x, y);
			if (stepViewIsCoupled) {
				this.setStepValueAction(index, this.stepValue(index).not);
			};
			stepPressedAction.value(this, index);
		};
		buttonReleasedAction = { |view, x, y|
			stepReleasedAction.value(this, this.prXyToIndex(x, y));
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
		# x, y = this.prIndexToXy(index);
		^this.buttonIsPressed(x, y);
	}

	stepIsReleased { |index|
		var x, y;
		# x, y = this.prIndexToXy(index);
		^this.buttonIsReleased(x, y);
	}

	value {
		^steps.copy
	}

	value_ { |val|
		this.validateValue(val);
		this.numSteps.do { |index| this.setStepValue(index, val[index]) }
	}

	valueAction_ { |val|
		var numStepValuesChanged;

		this.validateValue(val);
		numStepValuesChanged = 0;
		this.numSteps.do { |index|
			var newStepValue = val[index];
			if (this.stepValue(index) != newStepValue) {
				this.setStepValue(index, newStepValue);
				stepValueChangedAction.value(this, index, newStepValue);
				numStepValuesChanged = numStepValuesChanged + 1;
			}
		};
		if (numStepValuesChanged > 0) {
			this.doAction
		};
	}

	validateValue { |val|
		if (val.size != this.numSteps) {
			Error("value must be a 1-dimensional array of % values".format(this.numSteps)).throw
		}
	}

	stepsPressed {
		^this.buttonsPressed.collect { |button|
			this.prXyToIndex(button.x, button.y)
		}
	}

	stepValue { |index|
		^steps[index]
	}

	flashStep { |index, delay|
		var x, y;
		# x, y = this.prIndexToXy(index);
		this.flashButton(x, y, delay);
	}

	setStepValue { |index, val|
		steps[index] = val;
		if (this.prButtonValueByStepIndex(index) != (val or: (playhead == index))) {
			this.prSetButtonValueByStepIndex(index, val);
		}
	}

	setStepValueAction { |index, val|
		this.setStepValue(index, val);
		stepValueChangedAction.value(this, index, val);
		this.doAction;
	}

	numSteps {
		^this.numButtons
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

		if (playhead.notNil) {
			if (this.stepValue(playhead)) {
				this.flashStep(playhead, 100);
			} {
				this.prSetButtonValueByStepIndex(playhead, true);
			};
			if (previousPlayheadValue.notNil) {
				this.prRefreshStep(previousPlayheadValue);
			}
		} {
			if (previousPlayheadValue.notNil) {
				this.prRefreshStep(previousPlayheadValue);
			}
		};
	}

	prRefreshStep { |index|
		var x, y;
		var stepShouldBeLit;

		# x, y = this.prIndexToXy(index);
		stepShouldBeLit = this.stepValue(index) or: (index == playhead);

		if (this.buttonValue(x, y) != stepShouldBeLit) {
			this.setButtonValue(x, y, stepShouldBeLit);
		};
	}

	prXyToIndex { |x, y|
		^x + (y * this.numButtonCols)
	}

	prIndexToXy { |index|
		^[index mod: this.numButtonCols, index div: this.numButtonCols]
	}

	prButtonValueByStepIndex { |index|
		var x, y;
		# x, y = this.prIndexToXy(index);
		^this.buttonValue(x, y);
	}

	prSetButtonValueByStepIndex { |index, val|
		var x, y;
		# x, y = this.prIndexToXy(index);
		this.setButtonValue(x, y, val);
	}
}
