(
// core
var numKits = 4;
var numDrumsPerKit = 4 * 4;
var numStepsPerPattern = 8 * 4;
var numPatterns = 8 * 4;
var tempoSpec = ControlSpec(10, 800, step: 1, default: 120); // TODO: test to defer stuff on changed actions in order to circumvent late events when using high tempo
var swingAmountSpec = ControlSpec(0, 100, step: 1, default: 50);

var app = ();
var tempo;
var swingAmount;
var buffers;
var playhead=nil;
var currentPattern=0;
var queuedPattern=nil;

var kitSettings = numKits.collect {
	Array.fill(numDrumsPerKit, {
		(
			cutoff: 1.0,
			pitch: 0.5
		)
	});
};

var patterns = numPatterns.collect {
	(
		kit: 0,
		length: numStepsPerPattern,
		sequence: Array.fill([numDrumsPerKit, numStepsPerPattern], false)
	)
};

var setCurrentPattern = { |patternIndex|
	currentPattern = patternIndex;
	app.changed(\currentPattern, currentPattern);
};

var queuePattern = { |patternIndex|
	if (task.isPlaying) {
		setQueuedPattern.value(patternIndex);
	} {
		setCurrentPattern.value(patternIndex);
	}
};

var setQueuedPattern = { |patternIndex|
	queuedPattern = patternIndex;
	app.changed(\queuedPattern, queuedPattern);
};

var setPatternLength = { |patternIndex, length|
	patterns[patternIndex][\length] = length;
	app.changed(\patternLengthWasChanged, patternIndex, length);
};

var getPatternLength = { |patternIndex, length| patterns[patternIndex][\length] };

var startPlaying = {
	task.play;
	app.changed(\playing, true);
};

var stopPlaying = {
	task.stop;
	task.reset;
	setQueuedPattern.value(nil);
	app.changed(\playhead, nil);
	app.changed(\playing, false);
};

var toggleTrig = { |patternIndex, drumIndex, stepIndex|
	setTrig.value(patternIndex, drumIndex, stepIndex, getTrig.value(patternIndex, drumIndex, stepIndex).not);
};

var setKit = { |patternIndex, kitIndex|
	patterns[patternIndex][\kit] = kitIndex;
	app.changed(\kitWasChanged, patternIndex, kitIndex);
};

var getKit = { |patternIndex| patterns[patternIndex][\kit] };

var setTrig = { |patternIndex, drumIndex, stepIndex, trig|
	patterns[patternIndex][\sequence][drumIndex][stepIndex] = trig;
	app.changed(\trigWasChanged, patternIndex, drumIndex, stepIndex, trig);
};

var getTrig = { |patternIndex, drumIndex, stepIndex| patterns[patternIndex][\sequence][drumIndex][stepIndex] };

var drumSampleIsLoaded = { |kitIndex, drumIndex|
	buffers[kitIndex][drumIndex].path.notNil;
};

var loadKit = { |kitIndex, kit|
	var root = kit[\root];
	if (kit[\samples].size > numDrumsPerKit) {
		"only kits with % or less samples are allowed".format(numDrumsPerKit).error;
	} {
		kit[\samples].do { |filename, i| loadDrumSample.value(kitIndex, i, root +/+ filename) };
	}
};

var loadDrumSample = { |kitIndex, drumIndex, path|
	buffers[kitIndex][drumIndex].allocReadChannel(path, channels: [0], completionMessage: {}); // TODO: only mono channel ?
};

var setKitCutoff = { |kitIndex, drumIndex, val|
	kitSettings[kitIndex][drumIndex][\cutoff] = val;
};

var setKitPitch = { |kitIndex, drumIndex, val|
	kitSettings[kitIndex][drumIndex][\pitch] = val;
};

var getTempo = { tempo };

var getTempoSpec = { tempoSpec };

var adjustTempo = { |bpmDelta|
	setTempo.value(tempo + bpmDelta); // TODO: use step in ControlSpec?
};

var setTempo = { |bpm|
    tempo = tempoSpec.constrain(bpm);
	app.changed(\tempo, tempo);
    TempoClock.tempo = tempo/60;
};

var getSwingAmount = { swingAmount };

var getSwingAmountSpec = { swingAmountSpec };

var adjustSwingAmount = { |swingAmountDelta|
    setSwingAmount.value(swingAmount + swingAmountDelta); // TODO: use step in ControlSpec?
};

var setSwingAmount = { |amount|
    swingAmount = swingAmountSpec.constrain(amount);
	app.changed(\swingAmount, swingAmount);
};

var playStep = { |patternIndex, stepIndex|
	var pattern = patterns[patternIndex];
	numDrumsPerKit.do { |drumIndex|
		if (getTrig.value(currentPattern, drumIndex, stepIndex)) {
			playDrum.value(pattern[\kit], drumIndex);
		};
	};
};
var playDrum = { |kitIndex, drumIndex|
	var buffer = buffers[kitIndex][drumIndex];
	var kitSetting = kitSettings[kitIndex][drumIndex];

	s.bind {
		if (drumSampleIsLoaded.value(kitIndex, drumIndex)) {
			(
				instrument: if (buffer.numChannels == 1, 'drum1', 'drum2'),
				bufnum: buffer,
				cutoff: kitSetting[\cutoff],
				rate: kitSetting[\pitch]
			).play;
			app.changed(\drumWasPlayed, kitIndex, drumIndex);
		};
	};
};

var task = Task {
	var div = 0.25;
	inf.do { |tick|
		var now = tick.asInteger mod: patterns[currentPattern][\length];
		if ((now == 0) and: (queuedPattern.notNil)) {
			setCurrentPattern.value(queuedPattern);
			queuedPattern = nil;
		};
		app.changed(\playhead, now);

		playStep.value(currentPattern, now);

		(
			if (now.even,
				(100.0+swingAmount)/100,
				(100.0-swingAmount)/100
			) * div
		).wait;
	};
};

setTempo.value(tempoSpec.default);
setSwingAmount.value(swingAmountSpec.default);

// scsynth server resource management
SynthDef('drum1', { |out = 0, bufnum, cutoff, rate|
	OffsetOut.ar(
		out,
		Pan2.ar(
			RLPF.ar(
				PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum) * rate * 2, doneAction: 2),
				\freq.asSpec.map(cutoff)
			)
		)
	);
}).add;
SynthDef('drum2', { |out = 0, bufnum, cutoff, rate|
	OffsetOut.ar(
		out,
		RLPF.ar(
			PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * rate * 2, doneAction: 2),
			\freq.asSpec.map(cutoff)
		)
	);
}).add;

s.waitForBoot {
	buffers = Array.fill([numKits, numDrumsPerKit], { Buffer.new });
};

// logic
~app = app;
~getTempo = getTempo;
~getTempoSpec = getTempoSpec;
~adjustTempo = adjustTempo;
~setTempo = setTempo;
~getSwingAmount = getSwingAmount;
~getSwingAmountSpec = getSwingAmountSpec;
~adjustSwingAmount = adjustSwingAmount;
~setSwingAmount = setSwingAmount;
~loadKit = loadKit;
~playDrum = playDrum;
~getTrig = getTrig;
~toggleTrig = toggleTrig;
~startPlaying = startPlaying;
~stopPlaying = stopPlaying;
~queuePattern = queuePattern;
~setKit = setKit;
~getKit = getKit;
~setKitCutoff = setKitCutoff;
~setKitPitch = setKitPitch;
~setPatternLength = setPatternLength;
~getPatternLength = getPatternLength;
)

(
// user interface

var drumTrigNumCols = 4;
var drumTrigNumRows = 4;
var bottomPaneNumCols = 8;
var bottomPaneNumRows = 4;

var selectedPattern = 0;
var selectedDrum = 0;

var refreshStepEdit = {
	(bottomPaneNumCols*bottomPaneNumRows).do { |stepIndex|
		stepEdit.setStepValue(
			stepIndex,
			~getTrig.value(selectedPattern, selectedDrum, stepIndex)
		);
	};
};

var dw = GRMonome64('dw');

/*
	TODO
dw.gridConnectedAction = { |client, grid|
	"grid % is now used with DW.".format(grid).postln;
};
*/

var playButton = GRButton
	.newDecoupled(dw, 6@0, 1, 1)
	.buttonPressedAction_({
		~startPlaying.value;
	});

var stopButton = GRButton
	.newDecoupled(dw, 7@0, 1, 1)
	.buttonPressedAction_({
		~stopPlaying.value;
	})
	.value_(true);

var patternSelectModifier = GRButton
	.newDecoupled(dw, 7@3, 1, 1)
	.value_(true)
	.buttonPressedAction_({
		patternSelect.bringToFront;
	})
	.buttonReleasedAction_({
		stepEdit.bringToFront;
	});

var patternLengthModifier = GRButton
	.newDecoupled(dw, 6@3, 1, 1)
	.value_(true)
	.buttonPressedAction_({
		patternLength.bringToFront;
	})
	.buttonReleasedAction_({
		stepEdit.bringToFront;
	});

var kitSelect = GRHToggle
	.newDecoupled(dw, 4@1, 4, 1)
	.toggleValuePressedAction_({ |view, val|
		~setKit.value(selectedPattern, val);
	})
	.value_(~getKit.value(selectedPattern));

var drumTrig = GRStepView
	.newDecoupled(dw, 0@0, drumTrigNumCols, drumTrigNumRows)
	.stepPressedAction_({ |view, drumIndex|
		selectedDrum = drumIndex;
		drumTrig.playhead = drumIndex;
		~playDrum.value(kitSelect.value, drumIndex);
		refreshStepEdit.value;
	})
	.playhead_(selectedDrum);

var patternLength = GRHToggle
	.newDecoupled(dw, 0@4, bottomPaneNumCols, bottomPaneNumRows, false)
	.toggleValuePressedAction_({ |view, val|
		~setPatternLength.value(selectedPattern, val+1);
	})
	.thumbSize_([1, 1])
	.filled_(true)
	.value_((bottomPaneNumCols*bottomPaneNumRows)-1);

var patternSelect = GRStepView
	.newDecoupled(dw, 0@4, bottomPaneNumCols, bottomPaneNumRows, false)
	.stepPressedAction_({ |view, stepIndex|
		~queuePattern.value(stepIndex);
	})
	.playhead_(selectedPattern);

var stepEdit = GRStepView
	.newDecoupled(dw, 0@4, bottomPaneNumCols, bottomPaneNumRows)
	.stepPressedAction_({ |view, stepIndex|
		~toggleTrig.value(selectedPattern, selectedDrum, stepIndex);
	});

var listener = { |thechanged, what ... args|
	switch (what,
		\drumWasPlayed, {
			var kitIndex, drumIndex;
			# kitIndex, drumIndex = args;
			drumTrig.flashStep(drumIndex);
		},
		\trigWasChanged, {
			var patternIndex, drumIndex, stepIndex, value;
			# patternIndex, drumIndex, stepIndex, value = args;
			if ((patternIndex == selectedPattern) and: (drumIndex == selectedDrum)) {
				stepEdit.setStepValue(stepIndex, value);
			};
		},
		\kitWasChanged, {
			var patternIndex, kitIndex;
			# patternIndex, kitIndex = args;
			if (patternIndex == selectedPattern) {
				kitSelect.value = kitIndex;
			};
		},
		\playing, {
			var playing;
			# playing = args;
			playButton.value = playing;
			stopButton.value = playing.not;
		},
		\playhead, {
			var playhead;
			# playhead = args;
			stepEdit.playhead = playhead;
			if (playhead.notNil) {
				patternLength.flashToggleValue(playhead);
				if ((playhead mod: 4) == 0) {
					patternSelect.flashStep(selectedPattern);
				};
			};
		},
		\queuedPattern, {
			var pattern;
			# pattern = args;
			patternSelect.clear;
			if (pattern.notNil) {
				patternSelect.setStepValue(pattern, true);
			};
		},
		\currentPattern, {
			var pattern;
			# pattern = args;
			if (selectedPattern != pattern) {
				selectedPattern = pattern;
				refreshStepEdit.value;
				patternSelect.playhead = selectedPattern;
				patternSelect.setStepValue(selectedPattern, false);
				patternLength.value = ~getPatternLength.value(selectedPattern)-1;
				kitSelect.value = ~getKit.value(selectedPattern);
			};
		},
		\patternLengthWasChanged, {
			var patternIndex, length;
			# patternIndex, length = args;
			if (selectedPattern == patternIndex) {
				patternLength.value = length-1;
			};
		},
	);
};

refreshStepEdit.value;

[patternSelect, patternLength] do: _.enable;

~app.addDependant(listener);

dw.asSerialOscClient.onFree = {
	~app.removeDependant(listener);
};

//~screenGrid=dw.spawnGui.alwaysOnTop_(true);
//CmdPeriod.doOnce { ~screenGrid.remove }; // TODO: better done inside screedgrid

)
(
a=SerialOSCClient.enc("dw-encoders") { |client|
	var tempoX, swingAmountX;

	var listener = { |thechanged, what ... args|
		switch (what,
			\tempo, {
				refreshTempoEnc.value;
			},
			\swingAmount, {
				refreshSwingAmountEnc.value;
			}
		)
	};

	var refreshTempoEnc = {
		tempoX !? { |x| client.ringSet(0, x, 0) };
		tempoX = SerialOSCEnc.ledXSpec.map(~getTempoSpec.value.unmap(~getTempo.value));
		client.ringSet(0, tempoX, 15);
	};

	var refreshSwingAmountEnc = {
		swingAmountX !? { |x| client.ringSet(1, x, 0) };
		swingAmountX = SerialOSCEnc.ledXSpec.map(~getSwingAmountSpec.value.unmap(~getSwingAmount.value));
		client.ringSet(1, swingAmountX, 15);
	};

	client.encConnectedAction = { |client, enc|
		"encoder % is now used with DW.".format(enc).postln;
	};

	client.encRefreshAction = { |client|
		refreshTempoEnc.value;
		refreshSwingAmountEnc.value;
	};

	client.encDeltaAction = { |client, n, delta|
		// if kit key is held, adjust kit parameter
		// if step key is held, adjust param lock
		switch (n,
			0, {
				~adjustTempo.value(delta);
			},
			1, {
				~adjustSwingAmount.value(delta);
			}
		);
	};

	~app.addDependant(listener);

	client.onFree = {
		~app.removeDependant(listener);
	};
};
)

(
// load drum kits
~loadKit.value(
	0,
	(
		root: "C:/Users/AHorSE/OneDrive - IFS/Desktop/XR-20",
		samples: [
			"Kick/XR-20_023.wav",
			"Kick/XR-20_009.wav",
			"Rimshot/XR-20_257.wav",
			"Hats/XR-20_278.wav",
			"Hats/XR-20_290.wav",
			"Claps/XR-20_234.wav",
			"Perc/XR-20_626.wav",
			"Perc/XR-20_627.wav",
			"Snare/XR-20_123.wav",
			"Snare/XR-20_138.wav",
			"Hit/XR-20_427.wav",
			"Hit/XR-20_539.wav",
			"Hit/XR-20_501.wav",
			"Tom/XR-20_376.wav",
			"Tom/XR-20_377.wav",
			"Tom/XR-20_378.wav",
		]
	)
);
~loadKit.value(
	1,
	(
		root: "C:/Users/AHorSE/OneDrive - IFS/Desktop/XR-20",
		samples: [
			"Kick/XR-20_001.wav",
			"Kick/XR-20_002.wav",
			"Rimshot/XR-20_256.wav",
			"Hats/XR-20_288.wav",
			"Hats/XR-20_289.wav",
			"Claps/XR-20_235.wav",
			"Perc/XR-20_620.wav",
			"Perc/XR-20_621.wav",
			"Snare/XR-20_124.wav",
			"Snare/XR-20_133.wav",
			"Hit/XR-20_417.wav",
			"Hit/XR-20_531.wav",
			"Hit/XR-20_509.wav",
			"Tom/XR-20_371.wav",
			"Tom/XR-20_372.wav",
			"Tom/XR-20_373.wav",
		],
	)
);
)


(
SerialOSCGrid.rotation=90;
)



(
// record
s.record
)

(
)

(
EncDeltadef(\tempo).free
)

~queuePattern.value(0)

\freq.asSpec

~setKitCutoff.value(0, 7, 0.45)
~setKitPitch.value(0, 7, 0.60)
~setKitPitch.value(0, 7, 0.50)
~setKitPitch.value(0, 7, 0.35)

SerialOSCClient.postRoutings
SerialOSCClient.init
SerialOSCClient.initialized
SerialOSCClient.devices

SerialOSC.trace

TempoClock.tempo=200/60.0

s.latency

~app.dependants
