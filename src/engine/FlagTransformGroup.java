/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import javax.vecmath.Color3f;

class FlagTransformGroup extends ArmTransformGroup {
	Color3f jjColor;
	boolean direction;

	FlagTransformGroup() {
		this.direction = true;
	}
}
