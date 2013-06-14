GRSwitcher : GRContainerView {
	var
		<currentView
	;

	addChild { |view, origin|
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
		super.removeChild(view);
		if (view == currentView) {
			if (children.isEmpty) {
				currentView = nil;
			} {
				currentView = children.first;
			};
		}
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

	value_ { |index|
		var prevCurrentView, newCurrentView;

		if (index.isNil) {
			Error("it is not allowed to set switcher value to nil").throw;
		};
		if ((index < 0) or: (index >= children.size)) {
			Error("bad child index %. view has % children.".format(index, children.size)).throw;
		};
		if (this.value != index) {
			newCurrentView = children[index];
			if (currentView.notNil) {
				prevCurrentView = currentView;
				currentView = nil;
				prevCurrentView.disable;
			};
			newCurrentView.enable;
			currentView = newCurrentView;
		};
	}
}
