GRMonome64App : GRMonomeApp
{
	*new { |name, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(8, 8, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}

GRHMonome128App : GRMonomeApp
{
	*new { |name, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(16, 8, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}

GRVMonome128App : GRMonomeApp
{
	*new { |name, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(8, 16, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}

GRMonome256App : GRMonomeApp
{
	*new { |name, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(16, 16, name, view, origin, createTopViewIfNoneIsSupplied);
	}
}

GRMonomeApp : GRController
{
	classvar
		<all
	;

	var
		client,
		name
	;

	*new { |numCols, numRows, name, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(numCols, numRows, view, origin, createTopViewIfNoneIsSupplied).init(name);
	}

	*initClass {
		all = [];
	}

	init { |argName|
		var gridSpec;
		name = argName;
		gridSpec = (numCols: numCols, numRows: numRows);
		client = SerialOSCClient.new(name, gridSpec);
		client.gridRefreshAction = { this.refresh };
		client.gridKeyAction = { |client, x, y, state|
			if (this.containsPoint(x@y)) {
				this.emitButtonEvent(Point.new(x, y), state.asBoolean);
			} {
				"%x% is outside of current bounds: %x%".format(x, y, numCols, numRows).warn;
			};
		};
		client.willFree = { |client| this.remove };
		client.refreshGrid;

		all = all.add(this);
	}

	*newDetached {
		^this.new(nil, nil, false);
	}

	// TODO: probably move to init and this.onRemove_ = { client.free };
	onRemove { // TODO: test
		client.free;
	}

	handleViewLedRefreshedEvent { |point, on|
		client.ledSet(
			point.x.asInteger,
			point.y.asInteger,
			if (on.asBoolean, 1, 0)
		);
	}

	spawnGui {
		^GRScreenGrid.new(numCols, numRows, view, origin)
	}

	permanent {
		^client.permanent;
	}

	permanent_ { |argPermanent|
		client.permanent_(argPermanent);
	}

	grabDevices { client.grabDevices }
	grabGrid { client.grabGrid }
	asSerialOSCClient { ^client }
}
