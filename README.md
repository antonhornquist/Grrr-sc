# Grrr-sc

Grid controller UI widget library for SuperCollider

## Description

Simplifies interaction with for instance a Monome device using higher level UI abstractions

## Examples

### Example 1

a = ScreenGrid.new

b = GridButton.new(a, 0@0)

b.action = { |value| "the first button's value was changed to #{value}!".postln }

c = GridButton.newMomentary(a, 1@1, 2, 2)

c.action = { |value| "the second button's value was changed to #{value}!".postln }

a.removeAll

### Example 2

b = GridButton.newDecoupled(a, 0@0)

b.buttonPressedAction = { "the first button was pressed!".postln }

b.buttonReleasedAction = { "the first button was released!".postln }

## License

Copyright (c) Anton Hörnquist
