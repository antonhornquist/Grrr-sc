Monome64 : GRController
{
	classvar
		defaultHost="127.0.0.1",
		defaultHostPort=57120,
		defaultReceivePort=8080,
		defaultPrefix="/monome"
	;
	var
		<prefix,
		<host,
		<hostPort,
		<receivePort,
		oscFunc,
		target
	;

	*new { |prefix=nil, host=nil, hostPort=nil, receivePort=nil, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(8, 8, view, origin, createTopViewIfNoneIsSupplied).initMonome64(prefix, host, hostPort, receivePort);
	}

	initMonome64 { |argPrefix, argHost, argHostPort, argReceivePort|
		prefix = argPrefix ? defaultPrefix;
		host = argHost ? defaultHost;
		hostPort = argHostPort ? defaultHostPort;
		receivePort = argReceivePort ? defaultReceivePort;

		oscFunc = OSCFunc.newMatching(
			{
				|msg, time, addr, recvPort|
				this.emitButtonEvent(
					msg[1] @ msg[2],
					if (msg[3] == 1, true, false)
				);
			},
			(prefix ++ "/grid/key").asSymbol,
			recvPort: hostPort
		);

		target = NetAddr(host, receivePort);

		this.refresh;
	}

	*newDetached { |prefix=nil, host=nil, receivePort=nil|
		^this.new(prefix, host, receivePort, nil, nil, false);
	}

	cleanup {
		oscFunc.free;
	}

	// controller info
	info {
		^"Host: %
Host Port: %
Receive Port: %
Prefix: %".format(host, hostPort, receivePort, prefix)
	}

	// update monome leds
	handleViewLedRefreshedEvent { |point, on|
		target.sendMsg(
			(prefix ++ "/grid/led/set").asSymbol,
			point.x.asInteger,
			point.y.asInteger,
			if (on, 1, 0).asInteger
		);
	}

	// string representation
	asString {
		^super.asString+"(Prefix: \"%\", Host: %, Host Port: %, Receive Port: %)".format(prefix, host, hostPort, receivePort)
	}

	spawnGui {
		^GRScreenGrid.new(numCols, numRows, view, origin)
	}
}
