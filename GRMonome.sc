GRMonome : GRController {
	classvar
		<all
	;

	var
		<>onGridRouted,
		<>onGridUnrouted,
		client,
		name
	;

	*new { |numCols=16, numRows=8, name, view, origin, createTopViewIfNoneIsSupplied=true|
		^super.new(numCols, numRows, view, origin, createTopViewIfNoneIsSupplied).initGRMonome(name);
	}

	*initClass {
		all = [];
	}

	initGRMonome { |argName|
		var gridSpec;
		name = argName;
		gridSpec = (numCols: numCols, numRows: numRows);
		SerialOSCClient.new(name, gridSpec, \none) { |serialOSCClient|
			serialOSCClient.gridRefreshAction = { this.refresh };
			serialOSCClient.gridKeyAction = { |client, x, y, state|
				if (this.containsPoint(x@y)) {
					this.emitButtonEvent(Point.new(x, y), state.asBoolean);
				} {
					"%x% is outside of current bounds: %x%".format(x, y, numCols, numRows).warn;
				};
			};
			serialOSCClient.onFree = { this.remove };
			serialOSCClient.onGridRouted = { |client, grid| onGridRouted.value(this, grid); };
			serialOSCClient.onGridUnrouted = { |client, grid| onGridUnrouted.value(this, grid); };
			fork {
				0.5.wait;
				client = serialOSCClient;
				client.refreshGrid;
			}
		};

		all = all.add(this);
	}

	*newDetached { |numCols, numRows, name|
		^this.new(numCols, numRows, name, nil, nil, false);
	}

	handleViewLedRefreshedEvent { |point, on|
		client !? { |client|
			client.ledSet(
				point.x.asInteger,
				point.y.asInteger,
				if (on.asBoolean, 1, 0)
			);
		};
	}

	cleanup {
		client !? { |client|
			if (client.active) { client.free };
		};
	}

	spawnGui {
		^GRScreenGrid.new(numCols, numRows, view, origin)
	}

	grabDevices {
		client !? _.grabDevices;
	}

	grabGrid {
		client !? _.grabGrid;
	}

	asSerialOscClient {
		^if (client.notNil) { client } { Error("No client instantiated").throw };
	}
}
