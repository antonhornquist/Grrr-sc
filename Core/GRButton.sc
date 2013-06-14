GRButton : GRView {
	classvar
		<defaultNumCols=1,
		<defaultNumRows=1,
		<defaultFlashDelayWhenLit=25,
		<defaultFlashDelayWhenUnlit=50
	;

	var
		<>buttonPressedAction,
		<>buttonReleasedAction,
		<>behavior,
		>coupled,
		buttonWasPressed
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true, behavior=\toggle|
		^super.new(parent, origin, numCols ? defaultNumCols, numRows ? numCols ? defaultNumRows, enabled).initGRButton(coupled, behavior);
	}

	initGRButton { |argCoupled, argBehavior|
		coupled = argCoupled;
		behavior = argBehavior;
		value = false;
		isLitAtFunc = { value };
		viewButtonStateChangedAction = { |point, pressed|
			var buttonIsPressed;
			buttonIsPressed = this.isPressed;
			if (buttonWasPressed != buttonIsPressed) {
				if (coupled) {
					switch (behavior)
						{ \toggle } {
							if (buttonIsPressed) {
								this.toggleValue;
							};
						}
						{ \momentary } {
							this.toggleValue;
						};
				};
				if (buttonIsPressed) {
					buttonPressedAction !? buttonPressedAction.value(this);
				} {
					buttonReleasedAction !? buttonReleasedAction.value(this);
				};
				buttonWasPressed = buttonIsPressed;
			};
		}
	}

	*newDecoupled { |parent, origin, numCols, numRows|
		^this.new(parent, origin, numCols, numRows, true, false);
	}

	*newMomentary { |parent, origin, numCols, numRows|
		^this.new(parent, origin, numCols, numRows, true, true, \momentary);
	}

	isCoupled {
		^coupled
	}

	isPressed {
		^this.anyPressed
	}

	isReleased {
		^this.allReleased
	}

	flash { |delay|
		this.flashView(
			delay ? if (value, defaultFlashDelayWhenLit, defaultFlashDelayWhenUnlit )
		)
	}

	toggleValue {
		this.valueAction_(value.not)
	}
}
