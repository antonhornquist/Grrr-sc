/*

	MonomeSerial settings:

		I/O Protocol: OpenSound Control
		Host Address: monomeInstance.hostAddress
		Host Port: NetAddr.langPort (normally 57120)
		Listen Port: monomeInstance.listenPort
		Prefix: monomeInstance.prefix

*/

Monome64UsingMonomeSerial : GRController
{
	classvar
		defaultHostAddress="127.0.0.1",
		defaultListenPort=8080,
		defaultPrefix="/64"
	;
	var
		<prefix,
		<hostAddress,
		<listenPort,
		osc,
		target
	;

	*new { |prefix=nil, hostAddress=nil, listenPort=nil, view=nil, origin=nil, createTopViewIfNoneIsSupplied=true|
		^super.new(8, 8, view, origin, createTopViewIfNoneIsSupplied).initMonome64UsingMonomeSerial(prefix, hostAddress, listenPort);
	}

	initMonome64UsingMonomeSerial { |argPrefix, argHostAddress, argListenPort|
		hostAddress = argHostAddress ? defaultHostAddress;
		listenPort = argListenPort ? defaultListenPort;
		prefix = argPrefix ? defaultPrefix;

		osc = OSCresponder.new(nil, prefix ++ "/press", { |time, resp, msg|
			this.emitButtonEvent(
				msg[1] @ msg[2],
				if (msg[3] == 1, true, false)
			);
		});
		osc.add;

		target = NetAddr(hostAddress, listenPort);

		this.refresh;
	}

	*newDetached { |prefix=nil, hostAddress=nil, listenPort=nil|
		^this.new(prefix, hostAddress, listenPort, nil, nil, false);
	}

	cleanup {
		osc.remove;
	}

	// controller info
	info {
		^"MonomeSerial settings
=====================

I/O Protocol: OpenSound Control
Host Address: %
Host Port: %
Listen Port: %
Prefix: %".format(hostAddress, NetAddr.langPort, listenPort, prefix)
	}

	// update monome leds
	handleViewLedRefreshedEvent { |point, on|
		target.sendMsg(
			prefix ++ "/led",
			point.x.asInteger,
			point.y.asInteger,
			if (on, 1, 0).asInteger
		);
	}

	// string representation
	asString {
		^super.asString+"(Prefix: \"%\", Host Address: %, Host Port: %, Listen Port: %)".format(prefix, hostAddress, NetAddr.langPort, listenPort)
	}

	spawnGui {
		^GRScreenGrid.new(numCols, numRows, view, origin)
	}
}
