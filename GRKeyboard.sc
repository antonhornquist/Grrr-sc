GRKeyboard : GRView {
	classvar
		<minMidiNoteNumber=0,
		<maxMidiNoteNumber=127
	;

	var
		<>notePressedOnViewAction,
		<>noteReleasedOnViewAction,
		<>notePressedAction,
		<>noteReleasedAction,
		<>notShownNoteStateChangedAction,
		coupled,
		<>behavior,
		<basenote,
		<indicateKeys,
		notesPressedOnView,
		midiNoteLookup
	;

	*new { |parent, origin, numCols=7, numRows=2, enabled=true, basenote=60, coupled=true, behavior=\momentary|
		^super.new(nil, nil, numCols, numRows, enabled).initGRKeyboard(parent, origin, basenote, coupled, behavior)
	}

	initGRKeyboard { |argParent, argOrigin, argBasenote, argCoupled, argBehavior|
		this.validateMidiNote(argBasenote);
		basenote = argBasenote;
		this.prRecalculateMidiNoteLookup;

		coupled = argCoupled;
		behavior = argBehavior;
		indicateKeys = \blackAndWhite;
		notesPressedOnView = Array.new;

		value = ();
		this.midiNotesInterval.do { |note|
			value[note] = false;
		};

		viewButtonStateChangedAction = { |point, pressed|
			var note = this.getNoteAt(point);
			if (note.notNil) {
				if (pressed) {
					if(this.noteIsReleasedOnView(note)) {
						notePressedOnViewAction !? notePressedOnViewAction.value(this, note);
						notesPressedOnView = notesPressedOnView.add(note);
						if (coupled) {
							switch (behavior)
								{ \momentary } {
									this.setNotePressedAction(note);
								}
								{ \toggle } {
									this.toggleNoteAction(note);
								};
						}
					}
				} {
					if(this.noteIsPressedOnView(note)) {
						noteReleasedOnViewAction !? noteReleasedOnViewAction.value(this, note);
						notesPressedOnView.remove(note);
						if (coupled and: (behavior == \momentary)) {
							this.setNoteReleasedAction(note);
						}
					}
				}
			};
		};

		isLitAtFunc = { |point|
			var note, lit;
			note = this.getNoteAt(point);
			if (note.notNil) {
				lit = switch (indicateKeys)
					{ \blackAndWhite } { true }
					{ \black } { this.isBlackKey(note) }
					{ \white } { this.isBlackKey(note).not }
					{ \none } { false };
				if (this.noteIsPressed(note)) {
					lit.not
				} {
					lit
				};
			} {
				false
			}
		};

		// view has to be added to parent after class-specific properties
		// have been initialized, otherwise it is not properly refreshed
		this.validateParentOriginAndAddToParent(argParent, argOrigin);
	}

	*newDetached { |numCols=7, numRows=2, enabled=true, basenote=60, coupled=true, behavior=\momentary|
		^this.new(nil, nil, numCols, numRows, enabled, basenote, coupled, behavior)
	}

	*newDisabled { |parent, origin, numCols=7, numRows=2, basenote=60, coupled=true, behavior=\momentary|
		^this.new(parent, origin, numCols, numRows, false, basenote, coupled, behavior)
	}

	*newDecoupled { |parent, origin, numCols=7, numRows=2, enabled=true, basenote=60, behavior=\momentary|
		^this.new(parent, origin, numCols, numRows, enabled, basenote, false, behavior)
	}

	isCoupled {
		^coupled
	}

	coupled_ { |argCoupled|
		this.releaseAll;
		coupled = argCoupled;
	}

	basenote_ { |argBasenote|
		this.validateMidiNote(argBasenote);
		this.releaseAll;
		basenote = argBasenote;
		this.prRecalculateMidiNoteLookup;
		this.refresh;
	}

	indicateKeys_ { |argIndicateKeys|
		indicateKeys = argIndicateKeys;
		this.refresh;
	}

	getNoteAt { |point|
		^midiNoteLookup[point.x][point.y]
	}

	getPointOfNote { |note|
		^this.asPoints.detect { |point| this.getNoteAt(point) == note }
	}

	noteIsPressedOnView { |note|
		^notesPressedOnView.includes(note)
	}

	noteIsReleasedOnView { |note|
		^this.noteIsPressedOnView(note).not
	}

	noteIsPressed { |note|
		^value[note]
	}

	noteIsReleased { |note|
		^this.noteIsPressed(note).not
	}

	noteIs { |note, pressed|
		^value[note] == pressed
	}

	keyrange {
		^(this.minNoteShown to: this.maxNoteShown)
	}

	minNoteShown {
		^this.notesShown.minItem
	}

	maxNoteShown {
		^this.notesShown.maxItem
	}

	noteIsShown { |note|
		^this.notesShown.includes(note)
	}

	notesShown {
		^midiNoteLookup.flatten.select { |note| note.notNil }
	}

	isBlackKey { |note|
		^[1, 3, 6, 8, 10].includes(note mod: 12)
	}

	flashNote { |note, delay|
		if (this.noteIsShown(note)) {
			this.flashPoint(this.getPointOfNote(note), delay)
		}
	}

	flashNotes { |notes, delay|
		var shown;
		shown = notes.select { |note| this.noteIsShown(note) }.collect { |note| this.getPointOfNote(note) };

		if (shown.notEmpty) {
			this.flashPoints(shown, delay);
		};
	}

	flashLeftEdge { |delay|
		this.flashPoints(this.getLeftEdgePoints, delay);
	}

	flashRightEdge { |delay|
		this.flashPoints(this.getRightEdgePoints, delay);
	}

	getLeftEdgePoints {
		var minNoteShownPoint, otherPoint;
		minNoteShownPoint = this.getPointOfNote(this.minNoteShown);
		otherPoint = if (this.isBlackKey(this.minNoteShown)) {
			minNoteShownPoint + Point.new(0, 1);
		} {
			minNoteShownPoint + Point.new(0, -1);
		};
		^if (this.containsPoint(otherPoint)) {
			[minNoteShownPoint, otherPoint]
		} {
			[minNoteShownPoint]
		};
	}

	getRightEdgePoints {
		var maxNoteShownPoint, pointAbove;
		maxNoteShownPoint = this.getPointOfNote(this.maxNoteShown);
		pointAbove = maxNoteShownPoint + Point.new(0, -1);
		^if (this.containsPoint(pointAbove)) {
			[maxNoteShownPoint, pointAbove]
		} {
			[maxNoteShownPoint]
		}
	}

	prRecalculateMidiNoteLookup {
		var currentNote, currentRow;
		midiNoteLookup = Array.fill2D(numCols, numRows);
		currentNote = if (this.isBlackKey(basenote)) {
			basenote+1
		} {
			basenote
		};

		currentRow = numRows-1;
		while ({ currentRow >= 0 }) {
			numCols.do { |currentCol|
				var currentNoteFlat;

				if (this.isAllowedMidiNoteNumber(currentNote)) {
					midiNoteLookup[currentCol][currentRow] = currentNote;
				};

				currentNoteFlat = currentNote-1;

				if (this.containsPoint(Point.new(currentCol, currentRow-1)) and: this.isBlackKey(currentNoteFlat) and: this.isAllowedMidiNoteNumber(currentNoteFlat)) {
					midiNoteLookup[currentCol][currentRow-1] = currentNoteFlat;
				};

				currentNote = currentNote + if (this.isBlackKey(currentNote+1), 2, 1)
			};
			currentRow = currentRow - 2;
		};
	}

	toggleNoteAction { |note|
		this.prSetNote(note, this.noteIsPressed(note).not, true);
	}

	toggleNote { |note|
		this.prSetNote(note, this.noteIsPressed(note).not, false);
	}

	setNotePressedAction { |note|
		if (this.noteIsReleased(note)) {
			this.prSetNote(note, true, true);
		}
	}

	setNotePressed { |note|
		if(this.noteIsReleased(note)) {
			this.prSetNote(note, true, false);
		}
	}

	setNoteReleasedAction { |note|
		if (this.noteIsPressed(note)) {
			this.prSetNote(note, false, true);
		}
	}

	setNoteReleased { |note|
		if (this.noteIsPressed(note)) {
			this.prSetNote(note, false, false);
		}
	}

	setNoteAction { |note, pressed|
		if (this.noteIs(note, pressed.not)) {
			this.prSetNote(note, pressed, true);
		}
	}

	setNote { |note, pressed|
		if (this.noteIs(note, pressed.not)) {
			this.prSetNote(note, pressed, false);
		}
	}

	prSetNote { |note, pressed, triggerActions|
		var point;

		value[note] = pressed;

		if (this.noteIsShown(note)) {
			this.refreshPoint(this.getPointOfNote(note))
		} {
			this.handleNotShownNoteStateChange(note, pressed);
		};

		if (triggerActions) {
			if (pressed) {
				notePressedAction !? notePressedAction.value(this, note);
			} {
				noteReleasedAction !? noteReleasedAction.value(this, note);
			};
			this.doAction;
		};
	}

	handleNotShownNoteStateChange { |note, pressed|
		if (notShownNoteStateChangedAction.isKindOf(AbstractFunction)) {
			notShownNoteStateChangedAction.value(this, note, pressed);
		} {
			if (
				(notShownNoteStateChangedAction == \flashEdgeOnPress and: pressed)
				or:
				(notShownNoteStateChangedAction == \flashEdgeOnPressAndRelease)
			) {
				if (note < this.minNoteShown) {
					this.flashLeftEdge;
				} {
					if (note > this.maxNoteShown) {
						this.flashRightEdge;
					};
				};
			};
		};
	}

	valueAction_ { |argValue|
		var oldValue = value;
		if (value != argValue) {
			this.value_(argValue);
			this.midiNotesInterval.do { |note|
				if (oldValue[note] != value[note]) {
					if (value[note] == true) {
						notePressedAction !? notePressedAction.value(this, note);
					} {
						noteReleasedAction !? noteReleasedAction.value(this, note);
					};
				};
			};
			this.doAction;
		}
	}

	validateValue { |argValue|
		if ((argValue.size != 128) or: (argValue.keys.asArray.sort == this.midiNotesInterval.asArray).not) {
			Error("value must contain information for all 128 MIDI notes").throw
		};
	}

	validateMidiNote { |note|
		if (this.isAllowedMidiNoteNumber(note).not) {
			Error("invalid MIDI note: %".format(note)).throw
		};
	}

	isAllowedMidiNoteNumber { |note|
		^this.midiNotesInterval.includes(note)
	}

	midiNotesInterval {
		^(minMidiNoteNumber to: maxMidiNoteNumber)
	}
}
