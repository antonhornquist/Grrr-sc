GRHToggle : GRToggle {
	classvar
		<defaultNumCols=4,
		<defaultNumRows=1
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
			\horizontal
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
