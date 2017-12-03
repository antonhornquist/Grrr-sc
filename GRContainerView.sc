GRContainerView : GRView {
	var
		<pressThrough,
		children
	;

	*new { |parent, origin, numCols, numRows, enabled=true, pressThrough=false|
		^super.new(nil, nil, numCols, numRows, enabled).initGRContainerView(parent, origin, pressThrough);
	}

	initGRContainerView { |argParent, argOrigin, argPressThrough|
		pressThrough = argPressThrough;
		children = Array.new;

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols, numRows, enabled=true, pressThrough=false|
		^this.new(nil, nil, numCols, numRows, enabled, pressThrough);
	}

	*newDisabled { |parent, origin, numCols, numRows, pressThrough=false|
		^this.new(parent, origin, numCols, numRows, false, pressThrough);
	}

	// Parent - Child

	validateOkToAddChild { |view, origin|
		if (this.getParents.includes(view)) {
			Error("[%] is parent of [%]".format(view.asString, this.asString)).throw;
		};
		if (origin.isNil) {
			Error("origin is required").throw;
		};
		if (view.hasParent) {
			Error("[%] already has a parent".format(view.asString)).throw;
		};
		if ((origin.x < 0) or: (origin.y < 0)) {
			Error("child view origin may not be negative").throw;
		};
		this.validateWithinBounds(view, origin);
	}

	addChild { |view, origin|
		this.prAddChild(view, origin, false)
	}

	prAddChildNoFlash { |view, origin|
		this.prAddChild(view, origin, true)
	}

	prAddChild { |view, origin, preventFlash|
		this.validateOkToAddChild(view, origin);

		this.releaseAllWithinBounds(origin, view.numCols, view.numRows);

		children = children.add(view);

		view.setParentReference(this, origin);

		if (preventFlash.not and: GRCommon.indicateAddedRemovedAttachedDetached) {
			this.indicateBounds(view.origin, view.numCols, view.numRows)
		} {
			if (view.isEnabled) {
				view.refresh
			}
		};
	}

	removeAllChildren {
		this.prRemoveAllChildren(false)
	}

	prRemoveAllChildren { |preventFlash|
		children.copy.do { |child| this.prRemoveChild(child, preventFlash) }
	}

	removeChild { |view|
		this.prRemoveChild(view, false)
	}

	prRemoveChild { |view, preventFlash|
		this.validateParentOf(view);

		children.remove(view);

		if (preventFlash.not and: GRCommon.indicateAddedRemovedAttachedDetached) {
			this.indicateBounds(view.origin, view.numCols, view.numRows)
		} {
			if (view.isEnabled) {
				this.refreshBounds(view.origin, view.numCols, view.numRows)
			}
		};

		view.removeParentReference;
	}

	isParentOf { |view|
		^children.includes(view)
	}

	enabledChildren {
		^children.select(_.isEnabled)
	}

	hasChildAt { |point|
		^children.any { |view| view.containsPoint(point-view.origin) };
	}

	getChildrenAt { |point|
		^children.select { |view| view.containsPoint(point-view.origin) };
	}

	hasAnyEnabledChildAt { |point|
		^this.enabledChildren.any { |view| view.containsPoint(point-view.origin) };
	}

	getTopmostEnabledChildAt { |point|
		^this.enabledChildren.select { |view| view.containsPoint(point-view.origin) }.last;
	}

	isEmpty {
		^children.isEmpty
	}

	bringChildToFront { |view|
		children.remove(view);
		children = children.add(view);
		this.refreshBounds(view.origin, view.numCols, view.numRows)
	}

	sendChildToBack { |view|
		children.remove(view);
		children = children.insert(0, view);
		this.refreshBounds(view.origin, view.numCols, view.numRows)
	}

	// Validations

	validateWithinBounds { |view, origin|
		if ( this.isWithinBounds(view, origin).not ) {
			Error("[%] at % not within bounds of [%]".format(view, origin, this)).throw;
		};
	}

	isWithinBounds { |view, origin|
		^this.containsBounds(origin, view.numCols, view.numRows);
	}

	validateParentOf { |child|
		if (this.isParentOf(child).not) {
			Error("[%] is not parent of [%]".format(this, child)).throw;
		}
	}

	// Button events and state

	releaseAll {
		super.releaseAll;
		this.enabledChildren.do(_.releaseAll);
	}

	handleViewButtonEvent { |source, point, pressed|
		var view, respondingViews;
		if (enabled) {
			^if (this.hasAnyEnabledChildAt(point)) {
				view = this.getTopmostEnabledChildAt(point);

				if (GRCommon.traceButtonEvents) {
					"in % - button % at % (source: [%]) forwarded to [%] at %".format(
						thisMethod,
						if (pressed, "press", "release"),
						point,
						source,
						view,
						view.origin
					).postln
				};

				respondingViews = view.handleViewButtonEvent(source, point-view.origin, pressed);
				if (pressThrough) {
					respondingViews ++ super.handleViewButtonEvent(source, point, pressed);
				} {
					respondingViews
				}
			} {
				super.handleViewButtonEvent(source, point, pressed);
			}
		};
	}

	// Leds and refresh

	refreshPoint { |point, refreshChildren=true|
		var hasEnabledChildAtPoint, view;
		if (enabled) {
			hasEnabledChildAtPoint = this.hasAnyEnabledChildAt(point);
			if (hasEnabledChildAtPoint and: refreshChildren) {
				view = this.getTopmostEnabledChildAt(point);

				if (GRCommon.traceLedEvents) {
					"refresh at % forwarded to [%] at %".format(
						point,
						view,
						view.origin
					).postln
				};

				view.refreshPoint(point-view.origin);
			} {
				if (hasEnabledChildAtPoint.not) {
					super.refreshPoint(point);
				};
			};
		} {
			Error("view is disabled").throw;
		};
	}

	isLitAt { |point|
		var view;
		^if (this.hasAnyEnabledChildAt(point)) {
			view = this.getTopmostEnabledChildAt(point);

			if (GRCommon.traceLedEvents) {
				"isLitAt at % forwarded to [%] at %".format(
					point,
					view,
					view.origin
				).postln
			};

			view.isLitAt(point-view.origin);
		} {
			super.isLitAt(point);
		};
	}

	// String representation

	asPlot { |indentLevel=0|
		var
			delimiter = "    ",
			plotPressedLines,
			plotLedLines,
			plot,
			wrap;

		plotPressedLines = Array.fill(numRows) { Array.new };
		plotLedLines = Array.fill(numRows) { Array.new };
		this.asPoints.do { |point|
			wrap = if (this.hasAnyEnabledChildAt(point)) { [ "[", "]" ] } { [ " ", " " ] };
			plotPressedLines[point.y] = plotPressedLines[point.y].add(
				wrap.join(if (this.isPressedAt(point), 'P', '-'))
			);
			plotLedLines[point.y] = plotLedLines[point.y].add(
				wrap.join(if (this.isLitAt(point), 'L', '-'))
			);
		};
		plotPressedLines = plotPressedLines.collect { |row, i| i.asString ++ " " ++ row.join(" ") };
		plotLedLines = plotLedLines.collect { |row, i| i.asString ++ " " ++ row.join(" ") };
		plot = "  " ++ numCols.collect { |num| " " ++ num.asString ++ " " }.join(" ");
		plot = Array.fill(indentLevel, { "\t" }).join ++ plot ++ delimiter ++ plot ++ "\n";
		numRows.do { |i|
			plot = plot ++ Array.fill(indentLevel, { "\t" }).join ++ plotPressedLines[i] ++ delimiter ++ plotLedLines[i] ++ "\n"
		};
		^plot;
	}

	asTree { |includeDetails=false, indentLevel=0|
		^super.asTree(includeDetails, indentLevel) ++
			children.collect { |child|
				child.asTree(includeDetails, indentLevel+1)
			}.join("")
	}
}
