GRMonome256App : GRAbstractMonome {
	*new { |name, view, origin, createTopViewIfNoneIsSupplied=true|
		^super.new(16, 16, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}
