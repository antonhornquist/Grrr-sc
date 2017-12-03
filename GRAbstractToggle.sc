GRAbstractToggle : GRView {
	var
		>coupled,
		<orientation,
		<thumbWidth,
		<thumbHeight,
		nillable,
		valuesAreInverted,
		valuesPressed
	;

	*new { |numCols, numRows, enabled=true, coupled=true, nillable=false, orientation=\vertical|
		^super.new(nil, nil, numCols, numRows, enabled).initGRAbstractToggle(coupled, nillable, orientation);
	}

	initGRAbstractToggle { |argCoupled, argNillable, argOrientation|
		coupled = argCoupled;
		nillable = argNillable;
		orientation = argOrientation;
		valuesAreInverted = false;

		switch (orientation)
			{ \vertical } {
				thumbWidth = numCols;
				thumbHeight = 1;
			}
			{ \horizontal } {
				thumbWidth = 1;
				thumbHeight = numRows;
			};

		valuesPressed = Array.new;
	}

	isCoupled {
		^coupled
	}

	isNillable {
		^nillable
	}

	valuesAreInverted {
		^valuesAreInverted
	}

	valuesAreInverted_ { |argValuesAreInverted|
		this.releaseAll;
		valuesAreInverted = argValuesAreInverted;
		if (enabled) { this.refresh };
	}

	thumbSize {
		^[thumbWidth, thumbHeight]
	}

	thumbWidth_ { |argThumbWidth|
		this.thumbSize_([argThumbWidth, thumbHeight])
	}

	thumbHeight_ { |argThumbHeight|
		this.thumbSize_([thumbWidth, argThumbHeight])
	}

	thumbSize_ { |argThumbSize|
		this.validateThumbSize(argThumbSize);

		this.releaseAll;
		thumbWidth = argThumbSize[0];
		thumbHeight = argThumbSize[1];
		if (value > this.maximumValue) {
			value = 0
		};
		if (enabled) { this.refresh };
	}

	isPressed {
		^this.anyValuePressed
	}

	isReleased {
		^this.noValuePressed
	}

	valueIsPressed { |value|
		^valuesPressed.includes(value)
	}

	valueIsReleased { |value|
		^this.valueIsPressed(value).not
	}

	noValuePressed {
		^valuesPressed.isEmpty
	}

	anyValuePressed {
		^this.noValuePressed.not
	}

	firstValuePressed {
		^valuesPressed.first
	}

	lastValuePressed {
		^valuesPressed.last
	}

	minValuePressed {
		^valuesPressed.minItem
	}

	maxValuePressed {
		^valuesPressed.maxItem
	}

	numValuesPressed {
		^valuesPressed.size
	}

	maximumValue {
		^this.numValues-1
	}

	numValues {
		^this.numValuesX*this.numValuesY
	}

	numValuesX {
		^numCols div: thumbWidth
	}

	numValuesY {
		^numRows div: thumbHeight
	}

	valueAt { |point|
		var nonInvertedValue = switch (orientation)
			{ \vertical } { (point.y div: thumbHeight) + (point.x div: thumbWidth * this.numValuesY) }
			{ \horizontal } { (point.x div: thumbWidth) + (point.y div: thumbHeight * this.numValuesX) };

		^if (valuesAreInverted) {
			this.maximumValue - nonInvertedValue
		} {
			nonInvertedValue
		}
	}

	// Thumb size

	validateThumbSize { |argThumbSize|
		if (argThumbSize.size != 2) {
			Error("thumb size must be an array of two integers: [num_cols, num_rows].").throw
		};
		this.validateThumbWidth(argThumbSize[0]);
		this.validateThumbHeight(argThumbSize[1])
	}

	validateThumbWidth { |argThumbWidth|
		if (this.isValidThumbWidth(argThumbWidth).not) {
			Error(
				"% width (%) must be divisable by thumb width (%)".format(
					this.class,
					numCols,
					argThumbWidth
				)
			).throw
		}
	}

	validateThumbHeight { |argThumbHeight|
		if (this.isValidThumbHeight(argThumbHeight).not) {
			Error(
				"% height (%) must be divisable by thumb height (%)".format(
					this.class,
					numRows,
					argThumbHeight
				)
			).throw
		}
	}

	isValidThumbSize { |argThumbSize|
		^this.isValidThumbWidth(argThumbSize[0]) and: this.isValidThumbHeight(argThumbSize[1])
	}

	isValidThumbWidth { |argThumbWidth|
		^numCols % argThumbWidth == 0
	}

	isValidThumbHeight { |argThumbHeight|
		^numRows % argThumbHeight == 0
	}
}
