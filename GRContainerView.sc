GRContainerView : GRView {
	var
		<pressThrough,
		children,
		actsAsView
	;

	*new { |parent, origin, numCols, numRows, enabled=true, pressThrough=false|
		^super.new(nil, nil, numCols, numRows, enabled).initGRContainerView(parent, origin, pressThrough);
	}

	initGRContainerView { |argParent, argOrigin, argPressThrough|
		pressThrough = argPressThrough;
		children = Array.new;

		actsAsView = false;

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
		if (actsAsView) {
			Error("it is not allowed to add children to a %".format(this.class)).throw
		} {
			this.prAddChild(view, origin)
		}
	}

	prAddChildNoFlash { |view, origin|
		this.prAddChild(view, origin, true)
	}

	prAddChild { |view, origin, preventFlash=false|
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
		if (actsAsView) {
			Error("it is not allowed to remove children from a %".format(this.class)).throw
		} {
			this.prRemoveAllChildren
		}
	}

	prRemoveAllChildren { |preventFlash=false|
		children.copy.do { |child| this.prRemoveChild(child, preventFlash) }
	}

	removeChild { |view|
		if (actsAsView) {
			Error("it is not allowed to remove children from a %".format(this.class)).throw
		} {
			this.prRemoveChild(view)
		}
	}

	prRemoveChild { |view, preventFlash=false|
		if (this.isParentOf(view).not) {
			Error("[%] not a child of [%]".format(view, this)).throw;
		};

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
		children.move(this.prGetChildIndex(view), this.numChildren);
		this.refreshBounds(view.origin, view.numCols, view.numRows)
	}

	sendChildToBack { |view|
		children.move(this.prGetChildIndex(view), 0);
		this.refreshBounds(view.origin, view.numCols, view.numRows)
	}

	numChildren {
		^children.size;
	}

	prGetChildIndex { |argChild|
		^children.indexOf(argChild);
	}

	// Validations

	validateOkToEnableChild { |child|
	}

	validateOkToDisableChild { |child|
	}

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
			hasEnabledChildAtPoint = this.hasAnyEnabledChildAt(point); // UPCOMINGTODO: hasAnyEnabledChildrenAt
			if (hasEnabledChildAtPoint and: refreshChildren) {
				view = this.getTopmostEnabledChildAt(point); // UPCOMINGTODO: getTopmostEnabledChildAt

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

		if (actsAsView) {
			^super.asPlot(indentLevel)
		} {
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
	}

	asTree { |includeDetails=false, indentLevel=0|
		if (actsAsView) {
			^super.asTree(includeDetails, indentLevel)
		} {
			^super.asTree(includeDetails, indentLevel) ++
				children.collect { |child|
					child.asTree(includeDetails, indentLevel+1)
				}.join("")
		}
	}
}
