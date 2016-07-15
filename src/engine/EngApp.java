/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Vector;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

public class EngApp {
	protected TransformGroup viewTransform;

	public BranchGroup createSceneGraph(final Vector<boolean[]> top, final Vector<boolean[]> bottom,
			final Vector<String> variables) {
		final BranchGroup objRoot = new BranchGroup();
		final TransformGroup sceneTransform = new TransformGroup();
		((SceneGraphObject) sceneTransform).setCapability(17);
		((SceneGraphObject) sceneTransform).setCapability(18);
		((SceneGraphObject) sceneTransform).setCapability(14);
		((Group) objRoot).addChild((Node) sceneTransform);
		final Eng eng = new Eng(top, bottom, variables, this);
		final Transform3D trans = new Transform3D();
		trans.set(new Vector3f(eng.getCenterX(), 0.0f, 5.0f));
		this.viewTransform.setTransform(trans);
		((Group) sceneTransform).addChild((Node) eng.getBG());
		final BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
		final MouseRotate rotBehavior = new MouseRotate();
		((MouseBehavior) rotBehavior).setTransformGroup(sceneTransform);
		((Group) sceneTransform).addChild((Node) rotBehavior);
		((Behavior) rotBehavior).setSchedulingBounds((Bounds) bounds);
		final MouseZoom zoomBehavior = new MouseZoom();
		((MouseBehavior) zoomBehavior).setTransformGroup(sceneTransform);
		((Group) sceneTransform).addChild((Node) zoomBehavior);
		((Behavior) zoomBehavior).setSchedulingBounds((Bounds) bounds);
		final MouseTranslate transBehavior = new MouseTranslate();
		((MouseBehavior) transBehavior).setTransformGroup(sceneTransform);
		((Group) sceneTransform).addChild((Node) transBehavior);
		((Behavior) transBehavior).setSchedulingBounds((Bounds) bounds);
		objRoot.compile();
		return objRoot;
	}

	public EngApp(final Vector<boolean[]> top, final Vector<boolean[]> bottom, final Vector<String> variables) {
		this.viewTransform = null;
		final JFrame ef = new JFrame("Logic Engine");
		final Container ep = ef.getContentPane();
		ep.setLayout(new BorderLayout());
		final GraphicsDevice myDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		final GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
		template.setStereo(2);
		final GraphicsConfiguration gcfg = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getBestConfiguration((GraphicsConfigTemplate) template);
		final Canvas3D canvas3D = new Canvas3D(gcfg);
		((Component) canvas3D).setSize(800, 600);
		ep.add("Center", (Component) canvas3D);
		final SimpleUniverse simpleU = new SimpleUniverse(canvas3D);
		final PhysicalBody b = simpleU.getViewer().getPhysicalBody();
		b.setLeftEyePosition(new Point3d(-0.003, 0.0, 0.0));
		b.setRightEyePosition(new Point3d(0.003, 0.0, 0.0));
		final ViewingPlatform viewingPlatform = simpleU.getViewingPlatform();
		this.viewTransform = viewingPlatform.getViewPlatformTransform();
		final BranchGroup scene = this.createSceneGraph(top, bottom, variables);
		simpleU.addBranchGraph(scene);
		ef.pack();
		ef.setVisible(true);
	}
}
