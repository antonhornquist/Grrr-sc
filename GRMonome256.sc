GRMonome256 : GRMonome {
	*new { |name, view, origin, createTopViewIfNoneIsSupplied=true|
		^super.new(16, 16, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}
