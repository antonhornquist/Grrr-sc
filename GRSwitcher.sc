GRSwitcher : GRContainerView {
	var
		<currentView
	;

	*new { |parent, origin, numCols, numRows, enabled=true, pressThrough=false|
		^super.new(parent, origin, numCols, numRows, enabled, pressThrough);
	}

	addChild { |view, origin|
/*
	TODO: remove
		// TODO: below check added in conjunction with resolving monome repaint issues due to disabling and enabling views quickly. fix for when setting new value below could only be guaranteed to work when child view bounds are the same as GRSwitcher bounds which is why this guard clause was included for now. can be removed if *all* affected points are refreshed in one go after reenabling view led refreshed action
		if (((view.numCols == numCols) and: (view.numRows == numRows)).not) {
			Error("View added to GRSwitcher must be of same size as the GRSwitcher view: %x%".format(numCols, numRows)).throw;
		};
*/
		if (currentView.notNil) {
			if (view.isEnabled) {
				view.disable
			};
		} {
			if (view.isDisabled) {
				view.enable
			};
			currentView = view;
		};
		super.addChild(view, origin);
	}

	removeChild { |view|
		if (view == currentView) {
			if (children.size == 1) {
				currentView = nil;
			} {
				var currentValue;
				currentValue = this.value;
				if (currentValue == 0) {
					this.value_(1);
				} {
					this.value_(currentValue-1);
				}
			};
		};
		super.removeChild(view);
	}

	validateOkToEnableChild { |child|
		this.validateOkToEnableOrDisableChildren
	}

	validateOkToDisableChild { |child|
		this.validateOkToEnableOrDisableChildren
	}

	validateOkToEnableOrDisableChildren {
		if (currentView.notNil) {
			Error("it is not allowed to enable or disable children of a %. change value to switch between views.".format(this.class)).throw;
		}
	}

	switchToView { |view|
		this.validateParentOf(view);
		this.value_(children.indexOf(view));
	}

	switchTo { |id|
		var child;

		if (children.map { |child| child.id }.asSet.size != children.size) {
			Error("children in switcher do not have unique ids. it is not allowed to switch by id.").throw;
		};

		child = children.detect { |child| child.id == id };
		if (child.notNil) {
			this.value_(children.indexOf(child));
		} {
			Error("no child with id % in switcher.".format(id)).throw;
		};
	}

	value {
		^children.indexOf(currentView)
	}

/*
	value_ { |index|
		var prevCurrentView, newCurrentView;

		if (index.isNil) {
			Error("it is not allowed to set switcher value to nil").throw; // TODO: why? perhaps we should allow?
		};
		if ((index < 0) or: (index >= children.size)) {
			Error("bad child index %. view has % children.".format(index, children.size)).throw;
		};
		if (this.value != index) {
			newCurrentView = children[index];
			if (currentView.notNil) {
				prevCurrentView = currentView;
				currentView = nil;
				this.removeAction(parentViewLedRefreshedListener, \viewLedRefreshedAction); // TODO added to resolve monome repaint issues due to disabling and enabling views quickly. differing led repaint messages sent too quickly would not be handled sequentially and thus not yield the expected result. still a bug if child view bounds are not the same as GRSwitcher bounds why guard clause is included above
				prevCurrentView.disable;
				this.addAction(parentViewLedRefreshedListener, \viewLedRefreshedAction); // TODO: added to resolve monome repaint issues
			};
			newCurrentView.enable;
			currentView = newCurrentView;
		};
	}
*/
	value_ { |index|
		var prevCurrentView, newCurrentView;

		if (index.isNil) {
			Error("it is not allowed to set switcher value to nil").throw; // TODO: why? perhaps we should allow?
		};
		if ((index < 0) or: (index >= children.size)) {
			Error("bad child index %. view has % children.".format(index, children.size)).throw;
		};
		if (this.value != index) {
			this.doThenRefreshChangedLeds {
				newCurrentView = children[index];
				if (currentView.notNil) {
					prevCurrentView = currentView;
					currentView = nil;
					prevCurrentView.disable;
				};
				newCurrentView.enable;
				currentView = newCurrentView;
			};
		};
	}
}
