/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import javax.media.j3d.TransformGroup;

class ArmTransformGroup extends TransformGroup {
	double currentAngle;

	ArmTransformGroup() {
		this.currentAngle = 0.0;
	}
}
