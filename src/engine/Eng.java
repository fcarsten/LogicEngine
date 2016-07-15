/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import javax.media.j3d.Light;
import javax.media.j3d.Group;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.TextureAttributes;
import java.net.URL;
import java.io.IOException;
import java.awt.image.ImageProducer;
import java.awt.Toolkit;
import java.awt.Image;
import java.util.Iterator;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Box;
import javax.media.j3d.PointLight;
import javax.vecmath.Point3f;
import javax.media.j3d.SpotLight;
import javax.media.j3d.Bounds;
import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;
import javax.media.j3d.DirectionalLight;
import com.sun.j3d.utils.geometry.Cylinder;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;
import javax.media.j3d.Transform3D;
import java.awt.Component;
import com.sun.j3d.utils.image.TextureLoader;
import javax.media.j3d.Material;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Appearance;
import java.util.Vector;
import javax.vecmath.Color3f;

public class Eng {
	static final Color3f red;
	static final Color3f green;
	Vector<FlagTransformGroup>[] topFlags;
	Vector<FlagTransformGroup>[] bottomFlags;
	ArmTransformGroup[] armatures;
	Vector<Vector3f> chain_centres;
	Vector<boolean[]> topConfig;
	Vector<boolean[]> bottomConfig;
	Vector<String> variables;
	private Appearance barApp;
	private Appearance shaftApp;
	private BranchGroup BG;
	private float scaling_factor;
	private float frame_width;
	private float chain_radius;
	private float chain_gap;
	private float arm_width;
	private float shaft_radius;
	private float flag_height;
	private boolean[] topDirection;
	private boolean[] bottomDirection;
	static final Color3f white;
	static final Color3f black;
	private float centerX;

	static {
		red = new Color3f(1.0f, 0.0f, 0.0f);
		green = new Color3f(0.0f, 1.0f, 0.0f);
		white = new Color3f(1.0f, 1.0f, 0.6f);
		black = new Color3f(0.0f, 0.0f, 0.0f);
	}

	static Material createMaterial(final float r, final float g, final float b, final float s, final Color3f e) {
		final Material material = new Material();
		material.setDiffuseColor(r, g, b);
		material.setAmbientColor(r, g, b);
		if (e != null) {
			material.setEmissiveColor(e);
		}
		material.setShininess(s);
		((SceneGraphObject) material).setCapability(0);
		((SceneGraphObject) material).setCapability(1);
		material.setColorTarget(4);
		return material;
	}

	static Appearance createAppearance(final float r, final float g, final float b, final float s) {
		final Appearance appear = new Appearance();
		appear.setMaterial(createMaterial(r, g, b, s, null));
		return appear;
	}

	public Appearance createBarAppearance() {
		final Appearance woodLook = new ModulateTextureAppearance();
		final Color3f objColor = new Color3f(0.7f, 0.7f, 0.7f);
		final Material objMaterial = new Material(objColor, Eng.black, objColor, Eng.white, 100.0f);
		woodLook.setMaterial(objMaterial);
		final TextureLoader tex = new TextureLoader(this.grabImage("wood.jpg"), (Component) null);
		woodLook.setTexture(tex.getTexture());
		return woodLook;
	}

	public Appearance createRodAppearance() {
		final Appearance woodLook = new ModulateTextureAppearance();
		final Color3f objColor = new Color3f(0.7f, 0.7f, 0.7f);
		final Material objMaterial = new Material(objColor, Eng.black, objColor, Eng.white, 100.0f);
		woodLook.setMaterial(objMaterial);
		final TextureLoader tex = new TextureLoader(this.grabImage("goldrust.jpg"), (Component) null);
		woodLook.setTexture(tex.getTexture());
		return woodLook;
	}

	static Appearance createFlagAppearance(final float r, final float g, final float b, final float s) {
		final Appearance appear = new Appearance();
		appear.setMaterial(createMaterial(r, g, b, s, new Color3f(0.2f, 0.2f, 0.2f)));
		return appear;
	}

	public float getCenterX() {
		return this.centerX;
	}

	public void setCenterX(final float v) {
		this.centerX = v;
	}

	public void buildFrame(final BranchGroup BG) {
		final Transform3D translate = new Transform3D();
		final Transform3D rotate = new Transform3D();
		final int n = this.topConfig.size();
		final int m = this.topConfig.firstElement().length;
		final float engineLeftWidth = this.chain_radius + this.chain_gap / 4.0f + n * (this.arm_width * 5.0f);
		final float engineRightWidth = n * this.chain_radius * 2.0f + this.chain_gap * 0.75f + (n - 1) * this.chain_gap;
		final float engineWidth = engineLeftWidth + engineRightWidth;
		this.setCenterX(-engineLeftWidth + (engineLeftWidth + engineRightWidth) / 2.0f);
		float engineHeight = (m * 2.0f + this.shaft_radius * 2.0f) / this.scaling_factor;
		engineHeight += (n - 1) * this.arm_width * 10.0f;
		engineHeight += 2.0f * this.arm_width + 2.0f * this.chain_gap + 2.0f * this.frame_width;
		translate.set(new Vector3f(-engineLeftWidth + engineWidth / 2.0f, 0.0f, 0.0f));
		final TransformGroup shaftTGT = new TransformGroup(translate);
		((Group) BG).addChild((Node) shaftTGT);
		rotate.rotZ(1.5707963267948966);
		final TransformGroup shaftTGR = new TransformGroup(rotate);
		final Cylinder shaft = new Cylinder(this.shaft_radius, engineWidth, 3, this.shaftApp);
		((Group) shaftTGR).addChild((Node) shaft);
		((Group) shaftTGT).addChild((Node) shaftTGR);
		final DirectionalLight lightD1 = new DirectionalLight();
		((Light) lightD1).setInfluencingBounds((Bounds) new BoundingSphere(new Point3d(), 100.0));
		Vector3f direction1 = new Vector3f(0.0f, 0.0f, -1.0f);
		direction1.normalize();
		lightD1.setDirection(direction1);
		((Light) lightD1).setColor(new Color3f(0.15f, 0.15f, 0.15f));
		((Group) BG).addChild((Node) lightD1);
		final SpotLight lightP1 = new SpotLight();
		((Light) lightP1).setInfluencingBounds((Bounds) new BoundingSphere(new Point3d(), 100.0));
		((PointLight) lightP1).setPosition(new Point3f(-engineLeftWidth, -engineHeight / 2.0f, 7.0f));
		((Light) lightP1).setColor(new Color3f(1.0f, 0.3f, 0.0f));
		direction1 = new Vector3f(0.0f, 0.0f, -100.0f);
		direction1.normalize();
		lightP1.setDirection(direction1);
		((PointLight) lightP1).setAttenuation(0.0f, 0.0f, 0.02f);
		((Group) BG).addChild((Node) lightP1);
		final PointLight lightP2 = new PointLight();
		((Light) lightP2).setInfluencingBounds((Bounds) new BoundingSphere(new Point3d(), 100.0));
		lightP2.setPosition(new Point3f(0.0f, 0.0f, -5.0f));
		((Light) lightP2).setColor(new Color3f(0.4f, 0.4f, 1.0f));
		((Group) BG).addChild((Node) lightP2);
		translate.set(new Vector3f(engineRightWidth + this.frame_width, 0.0f, 0.0f));
		final TransformGroup frameTGT1 = new TransformGroup(translate);
		((Group) BG).addChild((Node) frameTGT1);
		final Box right_frame = new Box(this.frame_width, engineHeight / 2.0f, this.frame_width, 3, this.barApp);
		((Group) frameTGT1).addChild((Node) right_frame);
		translate.set(new Vector3f(-engineLeftWidth - this.frame_width, 0.0f, 0.0f));
		final TransformGroup frameTGT2 = new TransformGroup(translate);
		((Group) BG).addChild((Node) frameTGT2);
		final Box left_frame = new Box(this.frame_width, engineHeight / 2.0f, this.frame_width, 3, this.barApp);
		((Group) frameTGT2).addChild((Node) left_frame);
		translate.set(
				new Vector3f(-engineLeftWidth + engineWidth / 2.0f, -engineHeight / 2.0f + this.frame_width, 0.0f));
		final TransformGroup frameTGT3 = new TransformGroup(translate);
		((Group) BG).addChild((Node) frameTGT3);
		final Box bottom_frame = new Box(engineWidth / 2.0f, this.frame_width, this.frame_width, 3, this.barApp);
		((Group) frameTGT3).addChild((Node) bottom_frame);
		translate
				.set(new Vector3f(-engineLeftWidth + engineWidth / 2.0f, engineHeight / 2.0f - this.frame_width, 0.0f));
		final TransformGroup frameTGT4 = new TransformGroup(translate);
		((Group) BG).addChild((Node) frameTGT4);
		final Box top_frame = new Box(engineWidth / 2.0f, this.frame_width, this.frame_width, 3, this.barApp);
		((Group) frameTGT4).addChild((Node) top_frame);
	}

	public boolean buildFlags(final ArmTransformGroup armature, final Vector<FlagTransformGroup>[] theFlags,
			final float xpos, final float dir, final boolean[] directions, final boolean[] cells) {
		final Transform3D flagRotate = new Transform3D();
		final Transform3D translate = new Transform3D();
		boolean initialClash = false;
		final float flagDistance = cells.length / this.scaling_factor / (cells.length + 1);
		float currentFlagPos = flagDistance + this.shaft_radius;
		for (int i = 0; i < cells.length; ++i) {
			if (cells[i]) {
				boolean flagDirection = true;
				if (directions[i]) {
					flagRotate.rotZ(-1.5707963267948966);
					translate.set(new Vector3f(xpos + this.flag_height / 2.0f, dir * currentFlagPos, 0.0f));
					initialClash = true;
				} else {
					flagRotate.rotZ(1.5707963267948966);
					translate.set(new Vector3f(xpos - this.flag_height / 2.0f, dir * currentFlagPos, 0.0f));
					flagDirection = false;
				}
				final TransformGroup flagTGT = new TransformGroup(translate);
				((SceneGraphObject) flagTGT).setCapability(12);
				final FlagTransformGroup flagSpin = new FlagTransformGroup();
				flagSpin.jjColor = Eng.green;
				((SceneGraphObject) flagSpin).setCapability(18);
				((SceneGraphObject) flagSpin).setCapability(12);
				flagSpin.direction = flagDirection;
				((Group) armature).addChild((Node) flagSpin);
				((Group) flagSpin).addChild((Node) flagTGT);
				final TransformGroup flagTGR = new TransformGroup(flagRotate);
				((SceneGraphObject) flagTGR).setCapability(12);
				final Appearance tmpA = createFlagAppearance(0.0f, 1.0f, 0.0f, 15.0f);
				((SceneGraphObject) tmpA).setCapability(0);
				final Cone flag = new Cone(this.chain_radius, this.flag_height, 1, tmpA);
				((SceneGraphObject) flag.getShape(0)).setCapability(14);
				((SceneGraphObject) flag.getShape(1)).setCapability(14);
				((Group) flagTGR).addChild((Node) flag);
				((Group) flagTGT).addChild((Node) flagTGR);
				theFlags[i].add(flagSpin);
			} else {
				directions[i] = false;
				theFlags[i].add(null);
			}
			currentFlagPos += flagDistance;
		}
		return initialClash;
	}

	@SuppressWarnings("unchecked")
	public boolean buildArmatures(final BranchGroup BG) {
		final int m = this.topConfig.firstElement().length;
		final int n = this.topConfig.size();
		float chain_length = (m * 2.0f + this.shaft_radius * 2.0f) / this.scaling_factor;
		final Iterator<boolean[]> topColumns = this.topConfig.iterator();
		final Iterator<boolean[]> bottomColumns = this.bottomConfig.iterator();
		this.armatures = new ArmTransformGroup[n];
		this.topFlags = new Vector[m];
		this.bottomFlags = new Vector[m];
		this.chain_centres = new Vector<>(n);
		for (int i = 0; i < m; ++i) {
			this.topFlags[i] = new Vector<>(n);
			this.bottomFlags[i] = new Vector<>(n);
		}
		boolean initialClash = false;
		final Transform3D rotate = new Transform3D();
		final Transform3D translate = new Transform3D();
		float xposRight = this.chain_gap / 4.0f + this.chain_radius;
		float xposLeft = this.chain_radius + this.chain_gap / 4.0f;
		for (int chain_no = 0; chain_no != n; ++chain_no) {
			final ArmTransformGroup armature = new ArmTransformGroup();
			((SceneGraphObject) armature).setCapability(18);
			((SceneGraphObject) armature).setCapability(12);
			((Group) BG).addChild((Node) (this.armatures[chain_no] = armature));
			this.chain_centres.add(new Vector3f(xposRight, 0.0f, 0.0f));
			translate.set(new Vector3f(xposRight, 0.0f, 0.0f));
			final TransformGroup chainTGT = new TransformGroup(translate);
			((Node) chainTGT).setCollidable(false);
			((Group) armature).addChild((Node) chainTGT);
			final Cylinder chain = new Cylinder(this.chain_radius, chain_length, 3, this.shaftApp);
			((Group) chainTGT).addChild((Node) chain);
			final boolean[] topCells = topColumns.next();
			final boolean[] bottomCells = bottomColumns.next();
			initialClash = this.buildFlags(armature, this.topFlags, xposRight, 1.0f, this.topDirection, topCells);
			initialClash = (this.buildFlags(armature, this.bottomFlags, xposRight, -1.0f, this.bottomDirection,
					bottomCells) || initialClash);
			if (chain_no != n - 1) {
				initialClash = false;
			}
			translate.set(new Vector3f(-xposLeft, 0.0f, 0.0f));
			final TransformGroup arm1TGT = new TransformGroup(translate);
			final Box arm1 = new Box(this.arm_width, chain_length / 2.0f, this.arm_width, 3, this.barApp);
			((Group) armature).addChild((Node) arm1TGT);
			((Group) arm1TGT).addChild((Node) arm1);
			translate.set(new Vector3f(-xposLeft + (xposLeft + xposRight) / 2.0f, chain_length / 2.0f + this.arm_width,
					0.0f));
			final TransformGroup arm2TGT = new TransformGroup(translate);
			final Box arm2 = new Box((xposLeft + xposRight) / 2.0f + this.arm_width, this.arm_width, this.arm_width, 3,
					this.barApp);
			((Group) armature).addChild((Node) arm2TGT);
			((Group) arm2TGT).addChild((Node) arm2);
			final String var = this.variables.get(chain_no);
			translate.set(new Vector3f(-0.02f * ((var.length() + 4.0f) / 2.0f), chain_length / 2.0f,
					this.arm_width + 0.001f));
			final TransformGroup armTextTGT = new TransformGroup(translate);
			final Text2D armText = new Text2D(var.concat(new String(" = 0")), new Color3f(0.0f, 0.0f, 0.0f), "Times",
					10, 1);
			((Group) armature).addChild((Node) armTextTGT);
			((Group) armTextTGT).addChild((Node) armText);
			translate.set(new Vector3f(-xposLeft + (xposLeft + xposRight) / 2.0f, -chain_length / 2.0f - this.arm_width,
					0.0f));
			final TransformGroup arm3TGT = new TransformGroup(translate);
			final Box arm3 = new Box((xposLeft + xposRight) / 2.0f + this.arm_width, this.arm_width, this.arm_width, 3,
					this.barApp);
			((Group) armature).addChild((Node) arm3TGT);
			((Group) arm3TGT).addChild((Node) arm3);
			translate.set(new Vector3f(-0.02f * ((var.length() + 4.0f) / 2.0f), -chain_length / 2.0f,
					-this.arm_width - 0.001f));
			final TransformGroup armText2TGT = new TransformGroup(translate);
			rotate.rotX(3.141592653589793);
			final TransformGroup armText2TGR = new TransformGroup(rotate);
			final Text2D armText2 = new Text2D(var.concat(new String(" = 1")), new Color3f(0.0f, 0.0f, 0.0f), "Times",
					10, 1);
			((Group) armature).addChild((Node) armText2TGT);
			((Group) armText2TGT).addChild((Node) armText2TGR);
			((Group) armText2TGR).addChild((Node) armText2);
			xposRight += this.chain_gap + 2.0f * this.chain_radius;
			xposLeft += this.arm_width * 5.0f;
			chain_length += this.arm_width * 10.0f;
		}
		return initialClash;
	}

	public Eng(final Vector<boolean[]> t, final Vector<boolean[]> b, final Vector<String> v, final EngApp ea) {
		this.barApp = this.createBarAppearance();
		this.shaftApp = this.createRodAppearance();
		this.scaling_factor = 10.0f;
		this.frame_width = 0.1f;
		this.chain_radius = 0.0185f;
		this.chain_gap = 0.2f;
		this.arm_width = 0.0225f;
		this.shaft_radius = 0.0125f;
		this.flag_height = this.chain_radius + 0.9f * this.chain_gap;
		this.topConfig = t;
		this.bottomConfig = b;
		this.variables = v;
		this.buildUniverse(ea);
	}

	public void buildUniverse(final EngApp ea) {
		((SceneGraphObject) (this.BG = new BranchGroup())).setCapability(13);
		((SceneGraphObject) this.BG).setCapability(14);
		final int m = this.topConfig.firstElement().length;
		final int n = this.topConfig.size();
		this.topDirection = new boolean[m];
		this.bottomDirection = new boolean[m];
		for (int i = 0; i != m; ++i) {
			this.topDirection[i] = true;
			this.bottomDirection[i] = true;
		}
		this.buildFrame(this.BG);
		final boolean initialClash = this.buildArmatures(this.BG);
		final Scheduler sched = new Scheduler(this, initialClash, ea);
		((Group) this.BG).addChild((Node) sched);
		this.BG.compile();
	}

	public BranchGroup getBG() {
		return this.BG;
	}

	Image grabImage(final String name) {
		try {
			final Toolkit tk = Toolkit.getDefaultToolkit();
			final URL url = this.getClass().getResource(name);
			if (url == null) {
				System.err.println("Could not find texture file: " + name);
				return null;
			}
			final Image image = tk.createImage((ImageProducer) url.getContent());
			if (image == null) {
				System.err.println("Could find but not load texture file: " + name);
				return null;
			}
			return image;
		} catch (IOException ex) {
			System.out.println("Exception getting image: " + ex);
			return null;
		}
	}

	class ModulateTextureAppearance extends Appearance {
		public ModulateTextureAppearance() {
			final TextureAttributes textureAttributes = new TextureAttributes();
			textureAttributes.setTextureMode(5);
			this.setTextureAttributes(textureAttributes);
		}
	}
}
