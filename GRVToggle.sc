GRVToggle : GRToggle {
	classvar
		<defaultNumCols=1,
		<defaultNumRows=4
	;

	*new { |parent, origin, numCols, numRows, enabled=true, coupled=true, nillable=false|
		^super.new(
			parent,
			origin,
			numCols ? defaultNumCols,
			numRows ? defaultNumRows,
			enabled,
			coupled,
			nillable,
			\vertical
		)
	}

	*newDecoupled { |parent, origin, numCols, numRows, enabled=true, nillable=false|
		^this.new(
			parent,
			origin,
			numCols,
			numRows,
			enabled,
			false,
			nillable
		)
	}

	*newDetached { |numCols, numRows, enabled=true, coupled=true, nillable=false|
		^this.new(
			nil,
			nil,
			numCols,
			numRows,
			enabled,
			coupled,
			nillable
			)
	}

	*newNillable { |parent, origin, numCols, numRows, enabled=true, coupled=true|
		^this.new(
			parent,
			origin,
			numCols,
			numRows,
			enabled,
			coupled,
			true
		)
	}
}
