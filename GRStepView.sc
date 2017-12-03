GRStepView : GRView {
	classvar
		<playheadFlashDelayWhenLit=100;

	var
		<>stepPressedAction,
		<>stepReleasedAction,
		<>stepValueChangedAction,
		<playhead,
		coupled,
		steps,
		multiButtonView
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true|
		^super.new(nil, nil, numCols, numRows, enabled).initGRStepView(parent, origin, coupled);
	}

	initGRStepView { |argParent, argOrigin, argCoupled|
		coupled = argCoupled;
		multiButtonView = GRMultiButtonView.newDetached(numCols, numRows, true, false, \toggle);
		multiButtonView.addAction({ |originatingButton, point, on|
			if (this.hasViewLedRefreshedAction) {
				viewLedRefreshedAction.value(this, point, on);
			};
		}, \viewLedRefreshedAction);
		this.addAction({ |point, pressed|
			multiButtonView.handleViewButtonEvent(this, point, pressed);
		}, \viewButtonStateChangedAction);
		multiButtonView.buttonPressedAction = { |view, x, y|
			var index = this.prXyToIndex(x, y);
			if (coupled) {
				this.setStepValueAction(index, this.stepValue(index).not);
			};
			stepPressedAction.value(this, index);
		};
		multiButtonView.buttonReleasedAction = { |view, x, y|
			stepReleasedAction.value(this, this.prXyToIndex(x, y));
		};

		steps = Array.fill(this.numSteps, false);

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols, numRows, enabled=true, coupled=true|
		^this.new(nil, nil, numCols, numRows, enabled, coupled)
	}

	*newDecoupled { |parent, origin, numCols, numRows, enabled=true|
		^this.new(parent, origin, numCols, numRows, enabled, false)
	}

	isLitAt { |point|
		this.validateContainsPoint(point);
		^multiButtonView.isLitAt(point);
	}

	stepIsPressed { |index|
		var point;
		point = this.prIndexToXy(index);
		^multiButtonView.buttonIsPressed(point.x, point.y);
	}

	stepIsReleased { |index|
		var point;
		point = this.prIndexToXy(index);
		^multiButtonView.buttonIsReleased(point.x, point.y);
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
		^multiButtonView.buttonsPressed.collect { |pos|
			this.prXyToIndex(pos.x, pos.y)
		}
	}

	stepValue { |index|
		^steps[index]
	}

	flashStep { |index, delay|
		var point;
		point = this.prIndexToXy(index);
		multiButtonView.flashButton(point.x, point.y, delay);
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
		^multiButtonView.numButtons
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
			if (this.stepValue(playhead) || (previousPlayheadValue==playhead)) {
				this.flashStep(playhead, playheadFlashDelayWhenLit);
			} {
				this.prSetButtonValueByStepIndex(playhead, true);
			};
		};
		if (previousPlayheadValue.notNil and: (previousPlayheadValue != playhead)) {
			this.prRefreshStep(previousPlayheadValue);
		};
	}

	prRefreshStep { |index|
		var point;
		var stepShouldBeLit;

		point = this.prIndexToXy(index);
		stepShouldBeLit = this.stepValue(index) or: (index == playhead);

		if (multiButtonView.buttonValue(point.x, point.y) != stepShouldBeLit) {
			multiButtonView.setButtonValue(point.x, point.y, stepShouldBeLit);
		};
	}

	prXyToIndex { |x, y|
		^x + (y * multiButtonView.numButtonCols)
	}

	prIndexToXy { |index|
		^(index mod: multiButtonView.numButtonCols)@(index div: multiButtonView.numButtonCols)
	}

	prButtonValueByStepIndex { |index|
		var point;
		point = this.prIndexToXy(index);
		^multiButtonView.buttonValue(point.x, point.y);
	}

	prSetButtonValueByStepIndex { |index, val|
		var point;
		point = this.prIndexToXy(index);
		multiButtonView.setButtonValue(point.x, point.y, val);
	}
}
