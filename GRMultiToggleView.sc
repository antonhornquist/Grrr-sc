GRMultiToggleView : GRView {
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
		toggles,
		containerView
	;

	*new { |parent, origin, numCols, numRows, orientation=\vertical, enabled=true, coupled=true, nillable=false|
		^super.new(nil, nil, numCols, numRows, enabled).initGRMultiToggleView(parent, origin, orientation, coupled, nillable)
	}

	initGRMultiToggleView { |argParent, argOrigin, argOrientation, argCoupled, argNillable|
		var firstToggle;

		orientation = argOrientation;
		coupled = argCoupled;
		nillable = argNillable;
		this.prSetNumTogglesDefaults;

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
		toggles.do { |toggle| toggle.nillable = argNillable };
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
			var newToggleValue = val[i];
			if (this.toggleValue(i) != newToggleValue) {
				this.setToggleValue(i, newToggleValue);
				toggleValueChangedAction !? toggleValueChangedAction.value(this, i, newToggleValue);
				numToggleValuesChanged = numToggleValuesChanged + 1;
			}
		};
		if (numToggleValuesChanged > 0) {
			this.doAction;
		};
	}

	validateValue { |val|
 		if (val.size != numToggles) { Error("array must be of size %".format(numToggles)).throw };
		numToggles.do { |i|
			toggles[i].validateValue(val[i]);
		}
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

	isLitAt { |point|
		this.validateContainsPoint(point);
		^containerView.isLitAt(point);
	}

	flashToggle { |i, delay|
		toggles[i].flash(delay)
	}

	flashView { |delay|
		this.numToggles.do { |i|
			toggles[i].flash(delay);
		}
	}

	flashPoints {
 		Error("not implemented for GRMultiToggleView").throw;
	}

	prReconstructChildren {
		this.releaseAll;

		this.prDoThenRefreshChangedLeds {
			containerView.prRemoveAllChildren(true);
			toggles = Array.fill(numToggles) { |i|
				var toggle, position;
				toggle = GRToggle.newDetached(this.toggleWidth, this.toggleHeight, true, coupled, nillable, orientation);

				if (valuesAreInverted.notNil) {
					toggle.valuesAreInverted = valuesAreInverted
				};

				if (this.thumbSize != [nil, nil]) {
					if (toggle.isValidThumbSize(this.thumbSize)) {
						toggle.thumbSize = this.thumbSize
					} {
						this.thumbSize_(toggle.thumbSize)
					}
				};

				position = if (orientation == \vertical) {
					Point.new(i*this.toggleWidth, 0)
				} {
					Point.new(0, i*this.toggleHeight)
				};

				this.prAddActions(toggle, i);

				containerView.prAddChildNoFlash(toggle, position);
				toggle;
			};
		};
	}

	prAddActions { |toggle, index|
		toggle.togglePressedAction = { |view|
			togglePressedAction !? togglePressedAction.value(this, index)
		};
		toggle.toggleReleasedAction = { |view|
			toggleReleasedAction !? toggleReleasedAction.value(this, index)
		};
		toggle.toggleValuePressedAction = { |view, affectedValue|
			toggleValuePressedAction !? toggleValuePressedAction.value(this, index, affectedValue)
		};
		toggle.toggleRangePressedAction = { |view, range|
			toggleRangePressedAction !? toggleRangePressedAction.value(this, index, range)
		};
		toggle.action = { |view, value|
			toggleValueChangedAction !? toggleValueChangedAction.value(this, index, value);
			this.doAction;
		};
	}

	prSetNumTogglesDefaults {
		numToggles = if (orientation == \vertical, numCols, numRows)
	}
}
