GRController {
	classvar
		<>default,
		<>initAction,
		<all
	;

	var
		<numCols,
		<numRows,
		<view,
		<origin,
		<>onRemove,
		isRemoved,
		bottomRight,
		viewButtonStateChangedListener,
		viewLedRefreshedListener
	;

	*new { |numCols, numRows, view, origin, createTopViewIfNoneIsSupplied=true|
		^super.new.initGRController(numCols, numRows ? numCols, view, origin, createTopViewIfNoneIsSupplied)
	}

	initGRController { |argNumCols, argNumRows, argView, argOrigin, argCreateTopViewIfNoneIsSupplied|
		if (argNumCols.isNil or: argNumRows.isNil) {
			Error("numCols and numRows are mandatory").throw;
		};
		numCols = argNumCols;
		numRows = argNumRows;

		if (argView.notNil and: argOrigin.notNil) {
			this.prAttach(argView, argOrigin)
		} {
			if (argView.notNil) {
				Error("if a view is supplied an origin must also be supplied").throw
			};
			if (argOrigin.notNil) {
				Error("if an origin is supplied a view must also be supplied").throw
			};
			if (argCreateTopViewIfNoneIsSupplied) {
				this.prAttach(GRTopView.new(argNumCols, argNumRows), Point.new(0, 0), false);
			};
		};
		isRemoved = false;
		all = all.add(this);
		initAction !? initAction.value(this);
	}

	*newDetached { |numCols, numRows|
		^this.new(numCols, numRows, nil, nil, false);
	}

	remove {
		this.cleanup;
		if (this.isAttached) { this.detach };
		all.remove(this);
		onRemove !? onRemove.value;
	}

	cleanup {
		// subclass responsibility
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

	isRemoved {
		^isRemoved
	}

	// Bounds

	numButtons {
		^numCols * numRows
	}

	asPoints {
		^GRView.boundsToPoints(Point.new(0, 0), numCols, numRows)
	}

	// Controller info

	printInfo {
		this.info.postln
	}

	info {
		// subclass responsibility
	}

	// Attaching and detaching

	isAttached {
		^view.notNil
	}

	isDetached {
		^view.isNil
	}

	attach { |argView, argOrigin|
		this.prAttach(argView, argOrigin);
		this.refresh;
		if (GRCommon.indicateAddedRemovedAttachedDetached) {
			this.indicateController
		};
	}

	prAttach { |argView, argOrigin|
		if (this.isAttached) {
			Error("[%] is already attached to a view".format(this)).throw;
		};

		if (argView.containsBounds(argOrigin, numCols, numRows).not) {
			Error("[%] at origin % not within bounds of view [%]".format(this, argOrigin, view)).throw;
		};

		view = argView;
		origin = argOrigin;

		if (origin == Point.new(0, 0) and: (numCols == view.numCols) and: numRows == view.numRows) {
			viewLedRefreshedListener = { |source, point, on|
				this.handleViewLedRefreshedEvent(point-origin, on);
			};
			viewButtonStateChangedListener = { |point, pressed|
				this.handleViewButtonStateChangedEvent(point-origin, pressed);
			};
		} {
			bottomRight = origin + Point.new(numCols-1, numRows-1);
			viewLedRefreshedListener = { |source, point, on|
				if (point.x.inclusivelyBetween(origin.x, bottomRight.x) and: point.y.inclusivelyBetween(origin.y, bottomRight.y)) {
					this.handleViewLedRefreshedEvent(point-origin, on);
				};
			};
			viewButtonStateChangedListener = { |point, pressed|
				if (point.x.inclusivelyBetween(origin.x, bottomRight.x) and: point.y.inclusivelyBetween(origin.y, bottomRight.y)) {
					this.handleViewButtonStateChangedEvent(point-origin, pressed);
				};
			};
		};
		this.addLedRefreshedAction(viewLedRefreshedListener);
		this.addButtonStateChangedAction(viewButtonStateChangedListener);
	}

	detach {
		var viewSaved, originSaved;

		if (this.isDetached) {
			Error("[%] is already detached".format(this)).throw;
		};
		this.removeButtonStateChangedAction(viewButtonStateChangedListener);
		this.removeLedRefreshedAction(viewLedRefreshedListener);

		viewSaved = view;
		originSaved = origin;
		view = nil;
		origin = nil;

		this.refresh;

		if (GRCommon.indicateAddedRemovedAttachedDetached) {
			viewSaved.indicateBounds(originSaved, numCols, numRows);
		};
	}

	emitPress { |point|
		this.emitButtonEvent(point, true)
	}

	emitRelease { |point|
		this.emitButtonEvent(point, false)
	}

	emitButtonEvent { |point, pressed|
		this.validateContainsPoint(point);
		if (this.isAttached) {
			view.handleViewButtonEvent(this, origin+point, pressed)
		}
	}

	isPressedByThisControllerAt { |point|
		this.validateContainsPoint(point);
		^if (this.isAttached) {
			view.isPressedBySourceAt(this, origin+point)
		} { false }
	}

	isReleasedByThisControllerAt { |point|
		^this.isPressedByThisControllerAt(point).not
	}

	isPressedAt { |point|
		this.validateContainsPoint(point);
		^if (this.isAttached) {
			view.isPressedAt(origin+point)
		} { false }
	}

	isReleasedAt { |point|
		^this.isPressedAt(point).not
	}

	isLitAt { |point|
		this.validateContainsPoint(point);
		^if (this.isAttached) {
			view.isLitAt(origin+point);
		} { false }
	}

	handleViewButtonStateChangedEvent { |point, pressed|
		// subclass responsibility
	}

	handleViewLedRefreshedEvent { |point, on|
		// subclass responsibility
	}

	refresh {
		this.asPoints.do { |point|
			this.handleViewButtonStateChangedEvent(point, this.isPressedAt(point));
			this.handleViewLedRefreshedEvent(point, this.isLitAt(point));
		};
	}

/*
	DOC:
	Indicates on a view that a Controller is attached. Flashes the Controller's bounds.
*/

	indicateController { |repeat, interval|
		this.validateControllerIsAttached;
		view.indicateBounds(origin, numCols, numRows, repeat, interval)
	}

	validateControllerIsAttached {
		this.isDetached.if {
			Error("controller [%] is not attached to a view".format(this)).throw
		}
	}

	// Convenience methods

	addButtonStateChangedAction { |function|
		view.addAction(function, \viewButtonStateChangedAction)
	}

	removeButtonStateChangedAction { |function|
		view.removeAction(function, \viewButtonStateChangedAction)
	}

	addLedRefreshedAction { |function|
		view.addAction(function, \viewLedRefreshedAction)
	}

	removeLedRefreshedAction { |function|
		view.removeAction(function, \viewLedRefreshedAction)
	}

	// Delegate to view

	addChild { |argView, argOrigin|
		view.addChild(argView, argOrigin)
	}

	removeChild { |argView|
		view.removeChild(argView)
	}

	plot {
		view.plot
	}

	plotTree {
		this.postTree(true)
	}

	postTree { |includeDetails=false|
		view.postTree(includeDetails)
	}
}
