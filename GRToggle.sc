GRToggle : GRAbstractToggle {
	var
		<>togglePressedAction,
		<>toggleReleasedAction,
		<>toggleValuePressedAction,
		<>toggleRangePressedAction,
		filled,
		savedRange
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true, nillable=false, orientation=\vertical|
		^super.new(numCols, numRows, enabled, coupled, nillable, orientation).initGRToggle(parent, origin);
	}

	initGRToggle { |argParent, argOrigin|
		value = 0;
		filled = false;

		isLitAtFunc = { |point|
			var valueAtPoint;
			if (value.isNil) {
				false
			} {
				valueAtPoint = this.valueAt(point);
				if (this.isFilled) {
					valueAtPoint <= value
				} {
					valueAtPoint == value
				}
			}
		};

		viewButtonStateChangedAction = { |point, pressed|
			var localNumValuesPressed, range;
			var affectedValue = this.valueAt(point);
			if (this.prViewButtonStateChangeAffectedValuesPressed) {
				if (pressed) {
					valuesPressed = valuesPressed.add(affectedValue);
					localNumValuesPressed = this.numValuesPressed;

					if (this.isCoupled) {
						if (this.isNillable and: affectedValue == value) {
							this.valueAction_(nil)
						} {
							this.valueAction_(affectedValue)
						}
					};

					toggleValuePressedAction !? toggleValuePressedAction.value(this, affectedValue);

					if (localNumValuesPressed == 1) {
						togglePressedAction !? togglePressedAction.value(this)
					};

					if (localNumValuesPressed > 1) {
						range = [this.minValuePressed, this.maxValuePressed];
						if (savedRange != range) {
							toggleRangePressedAction !? toggleRangePressedAction.value(this, range);
							savedRange = range
						}
					}
				} {
					valuesPressed.remove(affectedValue);
					if (this.noValuePressed) {
						toggleReleasedAction !? toggleReleasedAction.value(this)
					}
				}
			}
		};

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDecoupled { |parent, origin, numCols, numRows, enabled=true, nillable=false, orientation=\vertical|
		^this.new(parent, origin, numCols, numRows, enabled, false, nillable, orientation)
	}

	*newDetached { |numCols, numRows, enabled=true, coupled=true, nillable=false, orientation=\vertical|
		^this.new(nil, nil, numCols, numRows, enabled, coupled, nillable, orientation)
	}

	*newNillable { |parent, origin, numCols, numRows, enabled=true, coupled=true, orientation=\vertical|
		^this.new(parent, origin, numCols, numRows, enabled, coupled, true, orientation)
	}

	isFilled {
		^filled
	}

	filled_ { |argFilled|
		filled = argFilled;
		if (enabled) { this.refresh };
	}

	nillable_ { |argNillable|
		nillable = argNillable;

		if (nillable == false and: value == nil) {
			this.valueAction_(0)
		}
	}

	validateValue { |argValue|
		if (((nillable and: argValue == nil) or: (0..this.maximumValue).includes(argValue)).not) {
			Error("value must be %an integer between 0 and %".format(if (nillable, "nil or ", ""), this.maximumValue)).throw
		}
	}

	flash { |delay|
		var pointsToFlash = this.asPoints.select { |point|
			if (this.isFilled) {
				this.valueAt(point) <= value
			} {
				this.valueAt(point) == value
			}
		};
		this.flashPoints(pointsToFlash, delay)
	}

	flashToggleValue { |value, delay|
		var pointsToFlash = this.asPoints.select { |point|
			this.valueAt(point) == value
		};
		this.flashPoints(pointsToFlash, delay)
	}

	prViewButtonStateChangeAffectedValuesPressed {
		^valuesPressed.size != pointsPressed.collect { |point| this.valueAt(point) }.asSet.size
	}
}
