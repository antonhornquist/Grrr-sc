(
a=GRScreenGrid.new;
b=GRVToggle.new(a, 0@0);
b.prGetLedStateWithinBounds(0@0, 1, 4);
)

x=b.prGetLedStateWithinBounds(0@0, 1, 4);
y=b.prGetLedStateWithinBounds(0@0, 1, 4);

x sect: y

(
y.select { |pointLedState1|
	x.any { |pointLedState2|
		(pointLedState1.key == pointLedState2.key)
		and:
		(pointLedState1.value != pointLedState2.value)
	}
};
)

a=GRScreenGrid.new;
b=GRVToggle.new(a, 0@0);
(
b.doThenRefreshChangedLeds {
	b.value=1
}
)

GRCommon.traceLedEvents = true;


