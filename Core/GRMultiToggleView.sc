GRMultiToggleView : GRContainerView {
	var
		<>togglePressedAction,
		<>toggleReleasedAction,
		<>toggleValuePressedAction,
		<>toggleRangePressedAction,
		<>toggleValueChangedAction,
		<numToggles,
		<orientation,
		<thumbWidth,
		<thumbHeight,
		coupled,
		nillable,
		filled,
		valuesAreInverted,
		toggles
	;

	*new { |parent, origin, numCols, numRows, orientation=\vertical, enabled=true, coupled=true, nillable=false|
		^super.new(nil, nil, numCols, numRows, enabled, true).initGRMultiToggleView(parent, origin, orientation, coupled, nillable)
	}

	initGRMultiToggleView { |argParent, argOrigin, argOrientation, argCoupled, argNillable|
		var firstToggle;

		orientation = argOrientation;
		coupled = argCoupled;
		nillable = argNillable;
		this.prSetNumTogglesDefaults;

		actsAsView = true;

		this.prReconstructChildren;

		firstToggle = toggles.first;
		filled = firstToggle.isFilled;
		valuesAreInverted = firstToggle.valuesAreInverted;
		thumbWidth = firstToggle.thumbWidth;
		thumbHeight = firstToggle.thumbHeight;

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols, numRows, orientation=\vertical, enabled=true, coupled=true, nillable=false|
		^this.new(nil, nil, numCols, numRows, orientation, enabled, coupled, nillable)
	}

	*newDisabled { |parent, origin, numCols, numRows, orientation=\vertical, coupled=true, nillable=false|
		^this.new(parent, origin, numCols, numRows, orientation, false, coupled, nillable)
	}

	*newDecoupled { |parent, origin, numCols, numRows, orientation=\vertical, enabled=true, nillable=false|
		^this.new(parent, origin, numCols, numRows, orientation, enabled, false, nillable)
	}

	orientation_ { |argOrientation|
		orientation = argOrientation;
		this.prSetNumTogglesDefaults;
		this.prReconstructChildren;
	}

	isCoupled {
		^coupled
	}

	coupled_ { |argCoupled|
		toggles.do { |toggle| toggle.coupled = argCoupled };
		coupled = argCoupled;
	}

	isNillable {
		^nillable
	}

	nillable_ { |argNillable|
		toggles.each { |toggle| toggle.nillable = argNillable };
		nillable = argNillable;
	}

	isFilled {
		^filled
	}

	filled_ { |argFilled|
		toggles.do { |toggle| toggle.filled = argFilled };
		filled = argFilled;
	}

	valuesAreInverted {
		^valuesAreInverted
	}

	valuesAreInverted_ { |argValuesAreInverted|
		toggles.do { |toggle| toggle.valuesAreInverted = argValuesAreInverted };
		valuesAreInverted = argValuesAreInverted;
	}

	thumbSize {
		^[thumbWidth, thumbHeight]
	}

	thumbWidth_ { |argThumbWidth|
		toggles.each { |toggle| toggle.thumbWidth = argThumbWidth };
		thumbWidth = toggles.first.thumbWidth;
	}

	thumbHeight_ { |argThumbHeight|
		toggles.do { |toggle| toggle.thumbHeight = argThumbHeight };
		thumbHeight = toggles.first.thumbHeight;
	}

	thumbSize_ { |argThumbSize|
		toggles.do { |toggle| toggle.thumbSize = argThumbSize };
		thumbWidth = toggles.first.thumbWidth;
		thumbHeight = toggles.first.thumbHeight;
	}

	value {
		^toggles.collect { |toggle| toggle.value }
	}

	value_ { |val|
		this.validateValue(val);
		numToggles.do { |i|
			toggles[i].value = val[i]
		}
	}

	valueAction_ { |val|
		var numToggleValuesChanged;
		this.validateValue(val);
		numToggleValuesChanged = 0;
		numToggles.do { |i|
			var toggle = toggles[i];
			if (toggle.value != val[i]) {
				toggle.value = val[i];
				toggleValueChangedAction !? toggleValueChangedAction.value(this, i, toggle.value);
				numToggleValuesChanged = numToggleValuesChanged + 1;
			}
		};
		if (numToggleValuesChanged > 0) {
			this.doAction;
		};
	}

	validateValue { |val|
 		if (val.length != numToggles) { Error("array must be of size %".format(numToggles)).throw }
	}

	maximumToggleValue {
		^toggles.first.maximumValue
	}

	toggleValue { |i|
		^toggles[i].value
	}

	setToggleValue { |i, val|
		toggles[i].value = val
	}

	setToggleValueAction { |i, val|
		toggles[i].valueAction = val
	}

	numToggles_ { |argNumToggles|
		this.validateNumToggles(argNumToggles);
		numToggles = argNumToggles;
		this.prReconstructChildren;
	}

	validateNumToggles { |argNumToggles|
		if (this.isValidNumToggles(argNumToggles).not) {
			switch (orientation)
				{ \vertical } {
					Error("% width (%) must be divisable by number of toggles (%)".format(this.class, numCols, argNumToggles)).throw
				}
				{ \horizontal } {
					Error("% height (%) must be divisable by number of toggles (%)".format(this.class, numRows, argNumToggles)).throw
				}
		}
	}

	isValidNumToggles { |argNumToggles|
		^(if (orientation == \vertical, numCols, numRows)) % numToggles == 0
	}

	toggleWidth {
		^switch (orientation)
			{ \vertical } {
				numCols / numToggles
			}
			{ \horizontal } {
				numCols
			}
	}

	toggleHeight {
		^switch (orientation)
			{ \vertical } {
				numRows
			}
			{ \horizontal } {
				numRows / numToggles
			}
	}

	prReconstructChildren {
		this.releaseAll;
		this.prRemoveAllChildren(true);

		toggles = Array.fill(numToggles) { |i|
			var toggle, position;
			toggle = GRToggle.newDetached(this.toggleWidth, this.toggleHeight, true, coupled, nillable, orientation);
			if (valuesAreInverted.notNil) {
				toggle.valuesAreInverted = valuesAreInverted
			};
			if (this.thumbSize != [nil, nil]) { // TODO: what's this?
				if (toggle.isValidThumbSize(this.thumbSize)) {
					toggle.thumb_size = this.thumbSize
				} {
					this.thumbSize_(toggle.thumbSize)
				}
			};
			this.prAddActions(toggle, i);

			position = if (orientation == \vertical) {
				Point.new(i*this.toggleWidth, 0)
			} {
				Point.new(0, i*this.toggleHeight)
			};

			this.prAddChildNoFlash(toggle, position);
			toggle;
		}
	}

	prAddActions { |toggle, index|
		toggle.togglePressedAction = { |toggle|
			togglePressedAction !? togglePressedAction.value(this, index)
		};
		toggle.toggleReleasedAction = { |toggle|
			toggleReleasedAction !? toggleReleasedAction.value(this, index)
		};
		toggle.toggleValuePressedAction = { |toggle, affectedValue|
			toggleValuePressedAction !? toggleValuePressedAction.value(this, index, affectedValue)
		};
		toggle.toggleRangePressedAction = { |toggle, range|
			toggleRangePressedAction !? toggleRangePressedAction.value(this, index, range)
		};
		toggle.action = { |toggle, value|
			toggleValueChangedAction !? toggleValueChangedAction.value(this, index, value);
			this.doAction;
		};
	}

	prSetNumTogglesDefaults {
		numToggles = if (orientation == \vertical, numCols, numRows)
	}
}
