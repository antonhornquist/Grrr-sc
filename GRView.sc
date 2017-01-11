GRView {
	classvar
		<defaultNumCols=4,
		<defaultNumRows=4,
		<defaultIndicateRepeat=2,
		<defaultIndicateInterval=50,
		<defaultFlashDelay=25
	;

	var
		<parent,
		<origin,
		<numCols,
		<numRows,
		<>id,
		<viewLedRefreshedAction,
		<>action,
		isLitAtFunc,
		value,
		pointsPressed,
		invertedLedsMap,
		parentViewLedRefreshedListener,
		enabled,
		viewButtonStateChangedAction,
		viewWasEnabledAction,
		viewWasDisabledAction
	;

	*new { |parent, origin, numCols, numRows, enabled=true|
		^super.new.initGRView(parent, origin, numCols, numRows, enabled);
	}

	initGRView { |argParent, argOrigin, argNumCols, argNumRows, argEnabled|
		numCols = argNumCols ? defaultNumCols;
		numRows = argNumRows ? numCols ? defaultNumRows;
		if ( (numCols < 1) or: (numRows < 1) ) { Error("minimum size is 1x1").throw };

		enabled = argEnabled;

		pointsPressed = Array.new(numCols*numRows);
		invertedLedsMap = Array.fill2D(numCols, numRows) { false };

		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols, numRows, enabled=true|
		^this.new(nil, nil, numCols, numRows, enabled);
	}

	*newDisabled { |parent, origin, numCols, numRows|
		^this.new(parent, origin, numCols, numRows, false);
	}

	// Bounds

	numViewButtons {
		^numCols * numRows
	}

	asPoints {
		^this.asPointsFrom(Point.new(0, 0))
	}

	asPointsFromOrigin {
		^this.asPointsFrom(origin)
	}

	asPointsFrom { |argOrigin|
		^GRView.boundsToPoints(argOrigin, numCols, numRows)
	}

	*boundsToPoints { |origin, numCols, numRows|
		^Array.fill( numCols * numRows, { |i| ( origin.x + (i mod: numCols) ) @ ( origin.y + (i div: numCols) ) } );
	}

	*pointsSect { |argPoints1, argPoints2|
		^all {: point1, point1 <- argPoints1, point2 <- argPoints2, point1 == point2 }
	}

	*boundsContainPoint { |origin, numCols, numRows, point|
		^(origin.x <= point.x) and: (origin.y <= point.y) and: (point.x < (origin.x + numCols)) and: (point.y < (origin.y + numCols))
	}

	leftTopPoint {
		^Point.new(0, 0)
	}

	rightTopPoint {
		^Point.new(numCols-1, 0)
	}

	leftBottomPoint {
		^Point.new(0, numRows-1)
	}

	rightBottomPoint {
		^Point.new(numCols-1, numRows-1)
	}

	leftmostCol { // TODO: add test
		^0
	}

	rightmostCol { // TODO: add test
		^numCols-1
	}

	topmostRow { // TODO: add test
		^0
	}

	bottommostRow { // TODO: add test
		^numRows-1
	}

	// Validations

	validateContainsPoint { |point|
		if (this.containsPoint(point) == false) { 
			Error("point"+point+"not within bounds of"+this).throw;
		}
	}

	containsPoint { |point|
		^point.x.inclusivelyBetween(0, numCols-1) and: point.y.inclusivelyBetween(0, numRows-1)
	}

	validateContainsBounds { |argOrigin, argNumCols, argNumRows|
		if (this.containsBounds(argOrigin, argNumCols, argNumRows) == false) { 
			Error("bounds (origin:"+argOrigin++", numCols"+argNumCols++", numRows:"+argNumRows++") not within bounds of"+this).throw;
		}
	}

	containsBounds { |argOrigin, argNumCols, argNumRows|
		^( 0 <= argOrigin.x ) and: ( 0 <= argOrigin.y ) and: ( ( argOrigin.x + argNumCols ) <= numCols ) and: ( ( argOrigin.y + argNumRows ) <= numRows )
	}

	// Button Events and State

	press { |point|
		^this.handleViewButtonEvent(this, point, true)
	}

	release { |point|
		^this.handleViewButtonEvent(this, point, false)
	}

	releaseAll {
		this.asPoints.do { |point| if (this.isPressedAt(point)) { this.release(point) } }
	}

	releaseAllWithinBounds { |argOrigin, argNumCols, argNumRows|
		this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).do { |point| this.release(point) }
	}

	isPressedAt { |point|
		this.validateContainsPoint(point);
		^pointsPressed.indexOfEqual(point).notNil;
	}

	isReleasedAt { |point|
		^this.isPressedAt(point).not
	}

	isPressedAtSkipValidation { |point|
		^pointsPressed.indexOfEqual(point).notNil;
	}

	anyPressed {
		^pointsPressed.notEmpty
	}

	anyPressedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).notEmpty
	}

	anyReleased {
		^pointsPressed.size != this.numViewButtons
	}

	anyReleasedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).size != (argNumCols*argNumRows)
	}

	allPressed {
		^pointsPressed.size == this.numViewButtons
	}

	allPressedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).size == (argNumCols*argNumRows)
	}

	allReleased {
		^pointsPressed.isEmpty
	}

	allReleasedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).isEmpty
	}

	numPressed {
		^pointsPressed.size
	}

	numPressedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).size
	}

	numReleased {
		^this.numViewButtons-this.numPressed
	}

	numReleasedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^(argNumCols * argNumRows) - this.pointsPressedWithinBounds(argOrigin, argNumCols, argNumRows).size
	}

	firstPressed {
		^pointsPressed.first
	}

	lastPressed {
		^pointsPressed.last
	}

	leftmostColPressed {
		var points;
		points = this.leftmostPressed;
		^if (points.isEmpty) { nil } { points.first.x };
	}

	rightmostColPressed {
		var points;
		points = this.rightmostPressed;
		^if (points.isEmpty) { nil } { points.first.x };
	}

	topmostRowPressed {
		var points;
		points = this.topmostPressed;
		^if (points.isEmpty) { nil } { points.first.y };
	}

	bottommostRowPressed {
		var points;
		points = this.bottommostPressed;
		^if (points.isEmpty) { nil } { points.first.y };
	}

	leftmostPressed {
		var minX = pointsPressed.collect { |point| point.x }.minItem;
		^pointsPressed.select { |point| point.x == minX };
	}

	rightmostPressed {
		var maxX = pointsPressed.collect { |point| point.x }.maxItem;
		^pointsPressed.select { |point| point.x == maxX };
	}

	topmostPressed {
		var minY = pointsPressed.collect { |point| point.y }.minItem;
		^pointsPressed.select { |point| point.y == minY };
	}

	bottommostPressed {
		var maxY = pointsPressed.collect { |point| point.y }.maxItem;
		^pointsPressed.select { |point| point.y == maxY };
	}

	handleViewButtonEvent { |source, point, pressed|
		if (enabled) {
			this.validateContainsPoint(point);
			^if (this.isPressedAtSkipValidation(point) != pressed) {
				if (pressed) {
					pointsPressed.add(point)
				} {
					pointsPressed.removeAt(
						pointsPressed.detectIndex(_==point)
					)
				};
				viewButtonStateChangedAction.value(point, pressed);

				if (GRCommon.traceButtonEvents) {
					"in % - button % at % (source: [%]) handled in [%]".format(
						thisMethod,
						if (pressed, "press", "release"),
						point,
						source,
						this
					).postln
				};

				[ (view: this, point: point) ];
			} {
				if (GRCommon.traceButtonEvents) {
					"in % - button state is already % in [%] at % %".format(
						thisMethod,
						if (pressed, "pressed", "released"),
						this,
						point,
						if (viewButtonStateChangedAction.notNil) { "- viewButtonStateChangedAction was not invoked" } { "" }
					).postln
				};

				[];
			};
		};
	}

	pointsPressed {
		^pointsPressed.copy
	}

	pointsPressedWithinBounds { |argOrigin, argNumCols, argNumRows|
		^pointsPressed.select { |point| GRView.boundsContainPoint(argOrigin, argNumCols, argNumRows, point) }
	}

	// Leds and Refresh

	refresh { |refreshChildren=true|
		if (enabled) {
			this.asPoints.do( this.refreshPoint(_, refreshChildren) );
		} {
			Error("view is disabled").throw;
		};
	}

	refreshBounds { |argOrigin, argNumCols, argNumRows, argRefreshChildren=true|
		if (enabled) {
			this.validateContainsBounds(argOrigin, argNumCols, argNumRows);
			GRView.boundsToPoints(argOrigin, argNumCols, argNumRows).do( this.refreshPoint(_, argRefreshChildren) );
		} {
			Error("view is disabled").throw;
		};
	}

	refreshPoints { |points, refreshChildren=true|
		if (enabled) {
			points.do { |point| this.refreshPoint(point) }
		} {
			Error("view is disabled").throw;
		};
	}

	refreshPoint { |point, refreshChildren=true|
		if (enabled) {
			this.validateContainsPoint(point);
			if (this.hasViewLedRefreshedAction) {
				viewLedRefreshedAction.value(this, point, this.isLitAt(point));
			};
		} {
			Error("view is disabled").throw;
		};
	}

	isLitAt { |point|
		this.validateContainsPoint(point);
		^if (isLitAtFunc.notNil) {
			isLitAtFunc.value(point) != invertedLedsMap[point.x][point.y]
		} {
			false
		}
	}

	isUnlitAt { |point|
		^this.isLitAt(point).not
	}

	anyLit {
		^this.asPoints.any { |point| this.isLitAt(point) }
	}

	allLit {
		^this.asPoints.all { |point| this.isLitAt(point) }
	}

	anyUnlit {
		^this.asPoints.any { |point| this.isUnlitAt(point) }
	}

	allUnlit {
		^this.asPoints.all { |point| this.isUnlitAt(point) }
	}

	// Indicate support

/*
	DOC:
	Indicate schedules to set leds of a specific area or of a collection of points to first lit and the unlit. This process is repeated a specified number of times (repeat) and with a specified delay in milliseconds (interval). When done it refreshes the points. Leds will be affected even though they are covered by child views. This is mainly used to indicate added / detached views and attached / detached controllers.
*/

	indicateView { |repeat, interval|
		this.indicatePoints(this.asPoints, repeat, interval)
	}

	indicateBounds { |argOrigin, argNumCols, argNumRows, argRepeat, argInterval|
		this.indicatePoints(GRView.boundsToPoints(argOrigin, argNumCols, argNumRows), argRepeat, argInterval)
	}

	indicatePoint { |point, repeat, interval|
		this.indicatePoints([point], repeat, interval)
	}

	indicatePoints { |points, repeat, interval|
		var
			intervalInSeconds = (interval ? defaultIndicateInterval)/1000.0
		;

		{
			(repeat ? defaultIndicateRepeat).do {
				[true, false].do { |bool|
					if (this.hasViewLedRefreshedAction) {
						points.do { |point| viewLedRefreshedAction.value(this, point, bool) }
					};
					intervalInSeconds.wait;
				};
			};
			this.isEnabled.if { this.refreshPoints(points) }
		}.fork(AppClock);
	}

	// Flash support

	flashView { |delay|
		this.flashPoints(this.asPoints, delay)
	}

	flashBounds { |argOrigin, argNumCols, argNumRows, argDelay|
		this.flashPoints(GRView.boundsToPoints(argOrigin, argNumCols, argNumRows), argDelay)
	}

	flashPoint { |point, delay|
		this.flashPoints([point], delay)
	}

	flashPoints { |points, delay|
		this.prInvertLeds(points);
		this.prScheduleToResetLeds(
			points,
			(delay ? defaultFlashDelay)  / 1000.0
		);
	}

	prInvertLeds { |points|
		points.do { |point| invertedLedsMap[point.x][point.y] = true };
		this.isEnabled.if { this.refreshPoints(points) };
	}

	prScheduleToResetLeds { |points, delayInSeconds|
		{
			delayInSeconds.wait;
			points.do { |point| invertedLedsMap[point.x][point.y] = false };
			this.isEnabled.if { this.refreshPoints(points) }
		}.fork(AppClock);
	}

	// Enable / Disable View

	enable {
		this.enabled_(true)
	}

	disable {
		this.enabled_(false)
	}

	isEnabled {
		^enabled
	}

	isDisabled {
		^this.isEnabled.not
	}

	enabled_ { |bool|
		if (enabled == bool) {
			Error("already %".format(if (enabled, "enabled", "disabled"))).throw;
		};
		if (bool) {
			if (this.hasParent) {
				parent.validateOkToEnableChild(this);
				parent.releaseAllWithinBounds(origin, numCols, numRows);
			};
			enabled = true;
			this.refresh;
			viewWasEnabledAction.value(this);
		} {
			if (this.hasParent) {
				parent.validateOkToDisableChild(this);
			};
			this.releaseAll;
			enabled = false;
			if (this.hasParent) {
				parent.refreshBounds(origin, numCols, numRows);
			};
			viewWasDisabledAction.value(this);
		};
	}

	// Action and Value

	addAction { |function, selector=\action|
		if ((selector == \viewButtonStateChangedAction) or: (selector == \viewLedRefreshedAction)) {
			switch (selector)
				{ \viewButtonStateChangedAction } {
					viewButtonStateChangedAction = viewButtonStateChangedAction.addFunc(function)
				}
				{ \viewLedRefreshedAction } {
					viewLedRefreshedAction = viewLedRefreshedAction.addFunc(function)
				}
				{ \viewWasEnabledAction } {
					viewWasEnabledAction = viewWasEnabledAction.addFunc(function)
				}
				{ \viewWasDisabledAction } {
					viewWasDisabledAction = viewWasDisabledAction.addFunc(function)
				}
		} {
			this.perform(selector.asSetter, this.perform(selector).addFunc(function));
		}
	}

	removeAction { |function, selector=\action|
		if ((selector == \viewButtonStateChangedAction) or: (selector == \viewLedRefreshedAction)) {
			switch (selector)
				{ \viewButtonStateChangedAction } {
					viewButtonStateChangedAction = viewButtonStateChangedAction.removeFunc(function)
				}
				{ \viewLedRefreshedAction } {
					viewLedRefreshedAction = viewLedRefreshedAction.removeFunc(function)
				}
				{ \viewWasEnabledAction } {
					viewWasEnabledAction = viewWasEnabledAction.removeFunc(function)
				}
				{ \viewWasDisabledAction } {
					viewWasDisabledAction = viewWasDisabledAction.removeFunc(function)
				}
		} {
			this.perform(selector.asSetter, this.perform(selector).removeFunc(function));
		}
	}

/*
	DOC:
	Returns the current state. This will not evaluate the function assigned to action. 
*/

	value {
		^value
	}

/*
	DOC:
	Sets the view to display the state of a new value. This will not evaluate the function assigned to action.
*/

	value_ { |argValue|
		if (value != argValue) {
			this.validateValue(argValue);
			value = argValue;
			if (this.isEnabled) {
				this.refresh
			}
		}
	}

/*
	DOC:
	Sets the view to display the state of a new value, and evaluates action, if the value has changed. 
*/

	valueAction_ { |argValue|
		if (value != argValue) {
			this.value_(argValue);
			this.doAction;
		}
	}

	doAction {
		action.value(this, this.value);
	}

	validateValue { |argValue|
	}

	// Parent - Child

	validateParentOriginAndAddToParent { |argParent, argOrigin|
		if (argParent.notNil and: argOrigin.notNil) {
			argParent.addChild(this, argOrigin);
		} {
			if (argParent.notNil) {
				Error("if a parent is supplied an origin must also be supplied").throw;
			};
			if (argOrigin.notNil) {
				Error("if an origin is supplied a parent must also be supplied").throw;
			};
		};
	}

	remove {
		if (this.hasParent) {
			parent.removeChild(this);
		} {
			Error("% has no parent".format(this.asString)).throw;
		}
	}

	hasParent {
		^if (parent.notNil and: origin.notNil) { true } { false };
	}

	isDetached {
		^this.hasParent.not
	}

	hasViewLedRefreshedAction {
		^viewLedRefreshedAction.notNil
	}

	setParentReference { |argParent, argOrigin|
		if (this.hasParent) {
			Error("cannot set parent reference - [%] already has a parent".format(this.asString)).throw;
		};
		parent = argParent;
		origin = argOrigin;
		parentViewLedRefreshedListener = { |source, point, on|
			var reason;
			if (parent.hasViewLedRefreshedAction and: parent.isEnabled) {

				if (GRCommon.traceLedEvents) {
					"led % at % (source: [%]) forwarded to [%]".format(
						if (on, "on", "off"),
						point,
						source,
						parent
					).postln;
				};

				parent.viewLedRefreshedAction.value(source, point+origin, on);
			} {

				if (GRCommon.traceLedEvents) {
					reason = if (parent.hasViewLedRefreshedAction.not) {
						"parent has no viewLedRefreshedAction"
					} {
						if (parent.isEnabled.not) {
							"parent is disabled"
						}
					};
					"led % at % (source: [%]) not forwarded to [%] - %".format(
						if (on, "on", "off"),
						point,
						source,
						parent,
						reason
					).postln;
				}

			}
		};
		this.addAction(parentViewLedRefreshedListener, \viewLedRefreshedAction);
	}

	removeParentReference {
		this.removeAction(parentViewLedRefreshedListener, \viewLedRefreshedAction);
		parent = nil;
		origin = nil;
	}

	getParents {
		// nicked from SCView
		var parents, view;
		view = this;
		parents = Array.new;
		while({(view = view.parent).notNil},{ parents = parents.add(view)});
		^parents
	}

	// String representation

	asString {
		^super.asString + "(%%x%, %)".format(if (id.notNil) { id.asString++", " } { "" }, numCols, numRows, if (enabled) { "enabled" } { "disabled" });
	}

	plot {
		this.asPlot.postln
	}

	plotTree {
		this.postTree(true)
	}

	postTree { |includeDetails=false, indentLevel=0|
		this.asTree(includeDetails, indentLevel).postln
	}

	asPlot { |indentLevel=0|
		var
			delimiter = "    ",
			plotPressedLines,
			plotLedLines,
			plot;

		plotPressedLines = Array.fill(numRows) { Array.new };
		plotLedLines = Array.fill(numRows) { Array.new };
		this.asPoints.do { |point|
			plotPressedLines[point.y] = plotPressedLines[point.y].add(
				if (this.isPressedAt(point), 'P', '-')
			);
			plotLedLines[point.y] = plotLedLines[point.y].add(
				if (this.isLitAt(point), 'L', '-')
			);
		};
		plotPressedLines = plotPressedLines.collect { |row, i| i.asString ++ " " ++ row.join(" ") };
		plotLedLines = plotLedLines.collect { |row, i| i.asString ++ " " ++ row.join(" ") };
		plot = "  " ++ numCols.collect(_.asString).join(" ");
		plot = Array.fill(indentLevel, { "\t" }).join ++ plot ++ delimiter ++ plot ++ "\n";
		numRows.do { |i|
			plot = plot ++ Array.fill(indentLevel, { "\t" }).join ++ plotPressedLines[i] ++ delimiter ++ plotLedLines[i] ++ "\n"
		};
		^plot
	}

	asTree { |includeDetails=false, indentLevel=0|
		^Array.fill(indentLevel, { "\t" }).join ++ this.asString ++
			if (includeDetails) {
				"\n" ++ this.asPlot(indentLevel)
			} {
				""
			} ++ "\n"
	}
}
