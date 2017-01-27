a=GRScreenGrid.new;
b=GRButton.new(a, 0@0, 4, 4).value_(true);
c=GRButton.new(a, 1@1, 2, 2);
c

a.view.getTopmostEnabledChildAt(0@0)
a.view.getTopmostEnabledChildAt(1@1)

c.sendToBack
b.sendToBack
b.bringToFront
c.bringToFront

a.view
b.flash
c.flash
c.enable
b.indicateView
c.indicateView
