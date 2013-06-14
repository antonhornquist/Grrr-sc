GRAreaView : GRView {
	var <>action, <>areaSetAction;
	var <>coupled, <>momentary;
	var <value; // TODO: use bounds [origin, numCols, numRows] instead of Rect as value

	*new { |parent, origin, numCols=nil, numRows=nil, enabled=true, coupled=true, momentary=false|
		^super.new(parent, origin, numCols, numRows, enabled).initGRAreaView(coupled, momentary);
	}

	initGRAreaView { |argCoupled=true, argMomentary=false|
		coupled = argCoupled;
		momentary = argMomentary;
		isLitAtFunc = { |point|
			if (value.notNil) {
				point.x.inclusivelyBetween(value.left, value.right)
				and: 
				point.y.inclusivelyBetween(value.top, value.bottom)
			} {
				false
			};
		};
		viewButtonStateChangedAction = { |point, pressed|
			var area;
			if (pressed) {
				area = Rect.newSides(
					this.leftmostColPressed,
					this.topmostRowPressed,
					this.rightmostColPressed,
					this.bottommostRowPressed
				);
				if (coupled) {
					this.valueAction_(area);
				};
				areaSetAction !? areaSetAction.value(this, area);
//			} {
//				if (momentary and: this.allReleased) {
//					area = nil;
//				};
			};
		};
	}

	// TODO: reuse in other instances aswell?
	doAction {
		action !? action.value(this, value);
	}

	value_ { |val|
		if (value != val) { // TODO add test for value_action, should not trigger action if value is not changed
			value = val;
			this.refresh;
		};
	}

	valueAction_ { |val|
		if (value != val) { // TODO add test for value_action, should not trigger action if value is not changed
			this.value_(val);
			this.doAction;
		};
	}
}
