GRVMonome128App : GRMonomeApp {
	*new { |name, view, origin, createTopViewIfNoneIsSupplied=true|
		^super.new(8, 16, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}
