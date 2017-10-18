GRTopView : GRContainerView {
	var
		pointsPressedBySource
	;

	*new { |numCols, numRows, enabled=true|
		^super.new(nil, nil, numCols, numRows, enabled, true).initGRTopView;
	}

	initGRTopView {
		pointsPressedBySource = Array.fill2D(numCols, numRows) { Array.new }
	}

	*newDetached { |numCols, numRows, enabled=true| // same as *new, this is just to override superclass version
		^this.new(numCols, numRows, enabled);
	}

	*newDisabled { |numCols, numRows|
		^this.new(numCols, numRows, false);
	}

	// Parent - Child

	setParentReference { |argParent, argOrigin|
		Error("a % may not be added as a child to another view".format(this)).throw
	}

	// Button events and state

	isPressedBySourceAt { |source, point|
		^pointsPressedBySource[point.x][point.y].includes(source)
	}

	isReleasedBySourceAt { |source, point|
		^this.isPressedBySourceAt(source, point).not
	}

	isPressedByOneSourceAt { |point|
		^pointsPressedBySource[point.x][point.y].size == 1
	}

	isNotPressedByAnySourceAt { |point|
		^pointsPressedBySource[point.x][point.y].isEmpty
	}

	press { |point|
		Error("not available in %".format(this.class)).throw
	}

	release { |point|
		Error("not available in %".format(this.class)).throw
	}

	handleViewButtonEvent { |source, point, pressed|
		if (enabled) {

			if (GRCommon.traceButtonEvents) {
				"in % - button % at % (source: [%]) received.".format(
					thisMethod,
					if (pressed, "press", "release"),
					point,
					source
				).postln
			};

			if (pressed) {
				if (this.isReleasedBySourceAt(source, point)) {
					pointsPressedBySource[point.x][point.y] = pointsPressedBySource[point.x][point.y].add(source);

					if (GRCommon.traceButtonEvents) {
						"in % - source [%] not referenced in array - reference was added.".format(thisMethod, source).postln
					};

 					if (this.isPressedByOneSourceAt(point)) {
						super.handleViewButtonEvent(source, point, true)
					}
				}
			} {
				if (this.isPressedBySourceAt(source, point)) {
					pointsPressedBySource[point.x][point.y].remove(source);

					if (GRCommon.traceButtonEvents) {
						"in % - source [%] referenced in array - reference was removed.".format(thisMethod, source).postln
					};

 					if (this.isNotPressedByAnySourceAt(point)) {
						super.handleViewButtonEvent(source, point, false)
					}
				}
			}
		}
	}
}
