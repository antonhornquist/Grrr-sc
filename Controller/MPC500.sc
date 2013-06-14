/*
MPC500Grid : ScreenGrid
{
	var 
		<>debugMidi=false
	;

	*new { |midiChannel=1| ^super.new(3, 4).initMPC500Grid(midiChannel) }

	initMPC500Grid { |mpc500MidiChannel|
		var midiSetup;

		midiSetup = #[ 
			48, 49, 50,
			44, 45, 46,
			40, 41, 42,
			36, 37, 38,
		];

		MIDIIn.connectAll;
		MIDIIn.noteOn = { |uid, channel, note, velocity|
			var x, y, index;
			if (debugMidi) { "noteOn %, %, %, %".format(uid, channel, note, velocity).postln; };
			if (channel == (mpc500MidiChannel-1)) {
				index = midiSetup.indexOf(note);
				index !? {
					x = index mod: topView.numCols;
					y = index div: topView.numCols;
					topView.handlePress(this, x@y, true);
				};
			};
		};
		MIDIIn.noteOff = { |uid, channel, note, velocity|
			var x, y, index;
			if (debugMidi) { "noteOff %, %, %, %".format(uid, channel, note, velocity).postln; };
			if (channel == (mpc500MidiChannel-1)) {
				index = midiSetup.indexOf(note);
				index !? {
					x = index mod: topView.numCols;
					y = index div: topView.numCols;
					topView.handlePress(this, x@y, false);
				};
			};
		};
	}
}

*/
