/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import javax.media.j3d.TransformInterpolator;
import com.sun.j3d.utils.geometry.Primitive;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Group;
import javax.vecmath.Vector3f;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.Material;
import javax.media.j3d.Appearance;
import javax.vecmath.Color3f;
import javax.media.j3d.Node;
import javax.media.j3d.ColorInterpolator;
import com.sun.j3d.utils.geometry.Cone;
import javax.media.j3d.TransformGroup;
import java.util.Vector;
import java.net.URL;
import javax.media.j3d.Bounds;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import java.applet.Applet;
import java.awt.Toolkit;
import javax.media.j3d.Alpha;
import java.util.Iterator;
import javax.media.j3d.Interpolator;
import javax.media.j3d.BranchGroup;
import java.util.Enumeration;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedTime;
import java.applet.AudioClip;
import java.util.LinkedList;
import javax.media.j3d.Behavior;

public class Scheduler extends Behavior {
	private int[] value;
	private int[] result;
	private int solutions;
	private LinkedList<BranchGroup> behaviorList;
	private AudioClip squeak;
	private static final long turn_time = 1200L;
	private static final long pause_time = 500L;
	private static long startTime;
	private long currentTime;
	private Eng eng;
	private EngApp engApp;

	static {
		Scheduler.startTime = System.currentTimeMillis();
	}

	@Override
	public void initialize() {
		this.wakeupOn((WakeupCondition) new WakeupOnElapsedTime(1700L));
	}

	@Override
	public synchronized void processStimulus(final Enumeration criteria) {
		this.currentTime = System.currentTimeMillis();
		final Iterator<BranchGroup> iter = this.behaviorList.iterator();
		while (iter.hasNext()) {
			final BranchGroup bg = iter.next();
			boolean detach = true;
			for (int c = 0; c < ((Group) bg).numChildren(); ++c) {
				final Interpolator inter = (Interpolator) ((Group) bg).getChild(c);
				final Alpha alpha = inter.getAlpha();
				if (!alpha.finished()) {
					detach = false;
				}
			}
			if (detach) {
				bg.detach();
				iter.remove();
			}
		}
		if (this.nextChange()) {
			this.wakeupOn((WakeupCondition) new WakeupOnElapsedTime(1700L));
		} else {
			LogicEngine.outArea.append("Finished.\n");
			LogicEngine.outArea.append("Found " + this.solutions + " possible solutions\n");
		}
	}

	public int getSolutions() {
		return this.solutions;
	}

	public Scheduler(final Eng e, final boolean initialClash, final EngApp ea) {
		this.behaviorList = new LinkedList<>();
		this.eng = e;
		this.engApp = ea;
		final Toolkit tk = Toolkit.getDefaultToolkit();
		final URL url = this.getClass().getResource("1_23b.wav");
		this.squeak = Applet.newAudioClip(url);
		final int m = ((boolean[]) this.eng.topConfig.firstElement()).length;
		final int number = this.eng.topConfig.size();
		this.value = new int[number];
		this.result = new int[number];
		for (int i = 0; i != number; ++i) {
			this.value[i] = 0;
			this.result[i] = 0;
		}
		this.solutions = 0;
		this.printHeader(this.eng.variables);
		if (initialClash) {
			this.colourFlags(this.eng.topFlags);
			this.colourFlags(this.eng.bottomFlags);
			this.printValues(this.eng.variables, this.result, false);
		} else {
			this.printValues(this.eng.variables, this.result, true);
			++this.solutions;
		}
		this.setSchedulingBounds((Bounds) new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0));
	}

	public boolean nextChange() {
		this.squeak.stop();
		int counter;
		for (counter = 0; counter < this.value.length && this.value[counter] == 1; this.value[counter++] = 0) {
		}
		if (counter == this.value.length) {
			return false;
		}
		this.value[counter] = 1;
		int index = -1;
		for (counter = this.value.length - 1; counter >= 0; --counter) {
			int tmpI;
			if (counter == this.value.length - 1) {
				tmpI = this.value[counter];
			} else {
				tmpI = ((this.value[counter] != this.value[counter + 1]) ? 1 : 0);
			}
			if (this.result[counter] != tmpI) {
				if (index != -1) {
					System.err.println("Index error");
				}
				index = counter;
				this.result[counter] = tmpI;
			}
		}
		if (index == -1) {
			System.err.println("Index error");
		}
		this.rotateArmature(index);
		this.rotateFlags(this.eng.topFlags);
		this.rotateFlags(this.eng.bottomFlags);
		boolean clash = this.colourFlags(this.eng.topFlags);
		clash = (this.colourFlags(this.eng.bottomFlags) || clash);
		if (!clash) {
			this.printValues(this.eng.variables, this.result, true);
			++this.solutions;
		} else {
			this.printValues(this.eng.variables, this.result, false);
		}
		this.squeak.play();
		return true;
	}

	public boolean colourFlags(final Vector<FlagTransformGroup>[] theFlags) {
		int tRows = 0;
		boolean clash = false;
		final BranchGroup tmpG = new BranchGroup();
		((SceneGraphObject) tmpG).setCapability(17);
		((SceneGraphObject) tmpG).setCapability(12);
		this.behaviorList.add(tmpG);
		while (tRows < theFlags.length) {
			final Vector<FlagTransformGroup> rowOfFlags = theFlags[tRows++];
			final FlagTransformGroup currentFlag = rowOfFlags.lastElement();
			if (currentFlag != null) {
				Color3f newColor = null;
				if (currentFlag.direction) {
					clash = true;
				}
				if (currentFlag.direction && currentFlag.jjColor == Eng.green) {
					newColor = Eng.red;
				} else if (!currentFlag.direction && currentFlag.jjColor == Eng.red) {
					newColor = Eng.green;
				}
				if (newColor == null) {
					continue;
				}
				final TransformGroup tempTGT = (TransformGroup) ((Group) currentFlag).getChild(0);
				final TransformGroup tempTGR = (TransformGroup) ((Group) tempTGT).getChild(0);
				final Cone tempCone = (Cone) ((Group) tempTGR).getChild(0);
				final Appearance tempApp = ((Primitive) tempCone).getAppearance();
				final Material flagMat = tempApp.getMaterial();
				final Alpha colorAlpha = new Alpha(1, 1, 10L, 0L, 1200L, 0L, 0L, 0L, 0L, 0L);
				colorAlpha.setStartTime(this.currentTime);
				final Color3f initCol = currentFlag.jjColor;
				final ColorInterpolator colInt = new ColorInterpolator(colorAlpha, flagMat, initCol, newColor);
				((Behavior) colInt).setSchedulingBounds((Bounds) new BoundingSphere());
				((Group) tmpG).addChild((Node) colInt);
				currentFlag.jjColor = newColor;
			}
		}
		if (((Group) tmpG).numChildren() > 0) {
			tmpG.compile();
			((Group) this.eng.getBG()).addChild((Node) tmpG);
		}
		return clash;
	}

	public void rotateArmature(final int index) {
		final ArmTransformGroup arm = this.eng.armatures[index];
		final Transform3D rotate = new Transform3D();
		final Alpha armAlpha = new Alpha(1, 1, 10L, 0L, 1200L, 0L, 0L, 0L, 0L, 0L);
		armAlpha.setStartTime(this.currentTime);
		final RotationInterpolator armRotInt = new RotationInterpolator(armAlpha, (TransformGroup) arm);
		((Behavior) armRotInt).setSchedulingBounds((Bounds) new BoundingSphere());
		rotate.rotZ(-1.5707963267948966);
		armRotInt.setMinimumAngle((float) arm.currentAngle);
		armRotInt.setMaximumAngle((float) (arm.currentAngle + 3.141592653589793));
		arm.currentAngle = (arm.currentAngle + 3.141592653589793) % 6.283185307179586;
		((TransformInterpolator) armRotInt).setTransformAxis(rotate);
		final BranchGroup tmpG = new BranchGroup();
		((SceneGraphObject) tmpG).setCapability(17);
		((SceneGraphObject) tmpG).setCapability(12);
		this.behaviorList.add(tmpG);
		((Group) tmpG).addChild((Node) armRotInt);
		tmpG.compile();
		((Group) this.eng.getBG()).addChild((Node) tmpG);
		int topRows = 0;
		int bottomRows = 0;
		while (topRows < this.eng.topFlags.length && bottomRows < this.eng.bottomFlags.length) {
			final Vector<FlagTransformGroup> currentTopRow = this.eng.topFlags[topRows++];
			final Vector<FlagTransformGroup> currentBottomRow = this.eng.bottomFlags[bottomRows++];
			final FlagTransformGroup temp = currentTopRow.get(index);
			final FlagTransformGroup temp2 = currentBottomRow.get(index);
			currentTopRow.set(index, temp2);
			currentBottomRow.set(index, temp);
		}
	}

	public void rotateFlags(final Vector<FlagTransformGroup>[] theFlags) {
		int numRows = 0;
		final BranchGroup tmpG = new BranchGroup();
		this.behaviorList.add(tmpG);
		((SceneGraphObject) tmpG).setCapability(17);
		((SceneGraphObject) tmpG).setCapability(12);
		while (numRows < theFlags.length) {
			boolean rowDirection = true;
			final Vector<FlagTransformGroup> rowOfFlags = theFlags[numRows++];
			final Iterator<FlagTransformGroup> currentRow = rowOfFlags.iterator();
			int current_chain = 0;
			while (currentRow.hasNext()) {
				final FlagTransformGroup currentFlag = currentRow.next();
				if (currentFlag != null) {
					if (currentFlag.direction != rowDirection) {
						final Alpha flagAlpha = new Alpha(1, 1, 10L, 0L, 1200L, 0L, 0L, 0L, 0L, 0L);
						flagAlpha.setStartTime(this.currentTime);
						final RotationInterpolator flagRotInt = new RotationInterpolator(flagAlpha,
								(TransformGroup) currentFlag);
						((Behavior) flagRotInt).setSchedulingBounds((Bounds) new BoundingSphere());
						flagRotInt.setMinimumAngle((float) currentFlag.currentAngle);
						flagRotInt.setMaximumAngle((float) (currentFlag.currentAngle + 3.141592653589793));
						currentFlag.currentAngle = (currentFlag.currentAngle + 3.141592653589793) % 6.283185307179586;
						currentFlag.direction = !currentFlag.direction;
						final Transform3D axis = new Transform3D();
						axis.set((Vector3f) this.eng.chain_centres.get(current_chain));
						((TransformInterpolator) flagRotInt).setTransformAxis(axis);
						((Group) tmpG).addChild((Node) flagRotInt);
					}
				} else {
					rowDirection = false;
				}
				++current_chain;
			}
		}
		tmpG.compile();
		((Group) this.eng.getBG()).addChild((Node) tmpG);
	}

	private void printValues(final Vector<String> variables, final int[] values, final boolean sol) {
		int val = 0;
		final Iterator<String> var = variables.iterator();
		while (val < values.length && var.hasNext()) {
			final int varLength = var.next().length();
			final int temp = varLength / 2;
			for (int i = 0; i != temp; ++i) {
				LogicEngine.outArea.append(" ");
			}
			LogicEngine.outArea.append("" + values[val++]);
			for (int i = 0; i != temp; ++i) {
				LogicEngine.outArea.append(" ");
			}
			LogicEngine.outArea.append(" ");
			if (varLength % 2 == 1) {
				LogicEngine.outArea.append(" ");
			}
		}
		if (sol) {
			LogicEngine.outArea.append(" ... true ");
		} else {
			LogicEngine.outArea.append(" ... false ");
		}
		LogicEngine.outArea.append("\n");
	}

	void printHeader(final Vector<String> variables) {
		LogicEngine.outArea.append("Results of NAE3SAT: \n");
		final Iterator<String> var = variables.iterator();
		while (var.hasNext()) {
			LogicEngine.outArea.append(String.valueOf(var.next()) + "  ");
		}
		LogicEngine.outArea.append("\n");
	}
}
