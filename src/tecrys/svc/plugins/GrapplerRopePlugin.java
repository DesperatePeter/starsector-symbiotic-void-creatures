/*
Code by Xaiier
 */

package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static tecrys.svc.plugins.tentacle_render.screen;

public class GrapplerRopePlugin extends svcBaseKinematicRopePlugin {
    protected WeaponAPI attachedWeapon;
    protected DamagingProjectileAPI attachedProj;
    private CombatEntityAPI target;
    private Vector2f offset;
    private float impactFacing;
    private Color baseColor;
    private boolean deadAndFading = false;
    private float fadeLevel = 1.0f;



    public GrapplerRopePlugin(int pluginID, int elementCount, float trailWidth, boolean is_pd, WeaponAPI attachedWeapon, DamagingProjectileAPI attachedProj) {
        super(pluginID, elementCount, trailWidth, is_pd);
        this.attachedWeapon = attachedWeapon;
        this.attachedProj = attachedProj;
        this.target = null;
        this.is_pd = is_pd;
        //create segments
        for (int i = 0; i < elementCount; i++) {
            SegmentPoint point = new SegmentPoint();
            point.location.set(MathUtils.getRandomPointInCircle(attachedWeapon.getLocation(), 5f));
            segmentPoints.add(point);
        }
    }

    @Override
    protected void initialSetup() {
        //TODO: move segment creation here?

        super.initialSetup(); //just sets done
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (Global.getCombatEngine().isPaused()) return;
        if (attachedProj == null) return;
        if (attachedProj.isExpired() || attachedProj.isFading() || !attachedWeapon.getShip().isAlive() || (target != null && (target.getHullLevel() == 0f || target.isExpired()))) {
            //Global.getCombatEngine().addFloatingTextAlways(target.getLocation(), String.valueOf(target.getClass()), 25f, Color.white, null, 1f, 1f, 0.01f, 0f, 0f, 1f);
            this.kill();
        }

        if (deadAndFading) {
            fadeLevel = fadeLevel * 0.9f;
            if (fadeLevel < 0.01f) {
                Global.getCombatEngine().removePlugin(this);
            }
        }

        if (target != null) {
            ropeParams.segmentGoalLength = Math.max(3f, ropeParams.segmentGoalLength * 0.99f);
            //Global.getCombatEngine().addFloatingTextAlways(attachedWeapon.getLocation(), String.valueOf(ropeParams.segmentGoalLength), 25f, Color.white, null, 1f, 1f, 0.01f, 0f, 0f, 1f);

            //MissileSpecAPI spec = (MissileSpecAPI) attachedWeapon.getSpec().getProjectileSpec();
            SpriteAPI sprite = Global.getSettings().getSprite("graphics/missiles/breach_srm.png");
            Vector2f loc = new Vector2f(target.getLocation());
            Vector2f worldSpaceOffset = new Vector2f(offset);
            worldSpaceOffset = VectorUtils.rotate(worldSpaceOffset, target.getFacing());
            loc = Vector2f.add(worldSpaceOffset, loc, null);
            MagicRender.singleframe(sprite, loc, new Vector2f(sprite.getWidth(), sprite.getHeight()), impactFacing + target.getFacing() - 90f, Color.white, false);
        }

        //TODO: move this into computeStringPhysics
        segmentPoints.get(0).getLocation().set(attachedWeapon.getLocation());
        segmentPoints.get(0).getVelocity().set(attachedWeapon.getShip().getVelocity());
        if (!deadAndFading) {
            if (target == null) {
                segmentPoints.get(segmentPoints.size() - 1).getLocation().set(attachedProj.getLocation());
                segmentPoints.get(segmentPoints.size() - 1).getVelocity().set(attachedProj.getVelocity());
            } else {
                Vector2f o = new Vector2f(offset);
                o = VectorUtils.rotate(o, target.getFacing());
                o = Vector2f.add(target.getLocation(), o, null);
                segmentPoints.get(segmentPoints.size() - 1).getLocation().set(o);
                segmentPoints.get(segmentPoints.size() - 1).getVelocity().set(target.getVelocity());
            }
        }

        super.advance(amount, events);

        if (target != null) {
            applyRopeForces();
        }
    }

    private void applyRopeForces() {
        //other
        Vector2f force = segmentPoints.get(segmentPoints.size() - 1).accel; //force in world space
        force.scale(1f / target.getMass());
        force.scale(Misc.getDistance( attachedWeapon.getLocation(), target.getLocation()) / (ropeParams.segmentGoalLength * elementCount));
        //Global.getCombatEngine().addSmoothParticle(Vector2f.add((Vector2f) (new Vector2f(force)).scale(1000f), segmentPoints.get(segmentPoints.size() - 1).getLocation(), null), new Vector2f(), 10f, 10f, 0.01f, Color.MAGENTA);
        Vector2f off = new Vector2f(offset); //offset in ship space
        //target.setFacing(0f);
        off = VectorUtils.rotate(off, target.getFacing()); //offset in rotated ship space
        //Global.getCombatEngine().addSmoothParticle(Vector2f.add(new Vector2f(off), target.getLocation(), null), new Vector2f(), 50f, 10f, 0.01f, Color.blue);
        force = VectorUtils.rotate(force, -VectorUtils.getFacing(off)); //force in ship space
        //Global.getCombatEngine().addSmoothParticle(Vector2f.add((Vector2f) (new Vector2f(force)).scale(100f), segmentPoints.get(segmentPoints.size() - 1).getLocation(), null), new Vector2f(), 50f, 10f, 0.01f, Color.MAGENTA);
        Vector2f fr = new Vector2f(force.x, 0f); //radial force in ship space
        Vector2f ft = new Vector2f(0f, force.y); //tangential force in ship space
        fr = VectorUtils.rotate(fr, VectorUtils.getFacing(off)); //radial force in world space
        //ft = VectorUtils.rotate(ft, VectorUtils.getFacing(off)); //tangential force in world space

        target.getVelocity().set(Vector2f.add(target.getVelocity(), fr, null));
        target.setAngularVelocity(target.getAngularVelocity() + ft.y);

        //ship
        Vector2f force2 = segmentPoints.get(0).accel; //force in world space
        force2.scale(1f / attachedWeapon.getShip().getMass());
        force2.scale(Misc.getDistance( attachedWeapon.getLocation(), target.getLocation()) / (ropeParams.segmentGoalLength * elementCount));
        //Global.getCombatEngine().addSmoothParticle(Vector2f.add((Vector2f) (new Vector2f(force2)).scale(1000f), segmentPoints.get(0).getLocation(), null), new Vector2f(), 10f, 10f, 0.01f, Color.MAGENTA);
        Vector2f off2 = new Vector2f(attachedWeapon.getSlot().getLocation()); //offset in ship space
        //target.setFacing(0f);
        off2 = VectorUtils.rotate(off2, attachedWeapon.getShip().getFacing()); //offset in rotated ship space
        //Global.getCombatEngine().addSmoothParticle(Vector2f.add(new Vector2f(off), target.getLocation(), null), new Vector2f(), 50f, 10f, 0.01f, Color.blue);
        force2 = VectorUtils.rotate(force2, -VectorUtils.getFacing(off2)); //force in ship space
        //Global.getCombatEngine().addSmoothParticle(Vector2f.add((Vector2f) (new Vector2f(force)).scale(100f), segmentPoints.get(segmentPoints.size() - 1).getLocation(), null), new Vector2f(), 50f, 10f, 0.01f, Color.MAGENTA);
        Vector2f fr2 = new Vector2f(force2.x, 0f); //radial force in ship space
        Vector2f ft2 = new Vector2f(0f, force2.y); //tangential force in ship space
        fr2 = VectorUtils.rotate(fr2, VectorUtils.getFacing(off2)); //radial force in world space
        //ft = VectorUtils.rotate(ft, VectorUtils.getFacing(off)); //tangential force in world space

        fr2.scale(0.5f);
        ft2.scale(0.5f);

        attachedWeapon.getShip().getVelocity().set(Vector2f.add(attachedWeapon.getShip().getVelocity(), fr2, null));
        attachedWeapon.getShip().setAngularVelocity(attachedWeapon.getShip().getAngularVelocity() + ft2.y);
    }

    public void attach(CombatEntityAPI target, Vector2f point, float projFacing) {
        this.target = target;
        Vector2f o = Vector2f.sub(point, target.getLocation(), null);
        o = VectorUtils.rotate(o, -target.getFacing());
        offset = o;

        this.impactFacing = projFacing - target.getFacing();
    }

    @Override
    protected void computeStringPhysics(float amount) {
        super.computeStringPhysics(amount); //compute base mechanics *before* forcing fixed locations

        //noise
        for (int i = 0; i < segmentPoints.size(); i++) {
            Vector2f noise = new Vector2f(MathUtils.getRandomNumberInRange(0f, 5f), 0f);
            noise = VectorUtils.rotate(noise, MathUtils.getRandomNumberInRange(0f, 360f));
            Vector2f newVel = segmentPoints.get(i).getVelocity();
            newVel = Vector2f.add(newVel, noise, null);
            segmentPoints.get(i).getVelocity().set(newVel);
        }
    }

    @Override
    protected Color computeColorForSegment(int renderPointIndex) {
//        //TODO: figure out how to not have to recompute angles
//        ArrayList<Double> angles = new ArrayList<>();
//
//        if (renderPointIndex - 1 >= 0) { //if previous exists
//            Vector2f point1 = new Vector2f(renderPoints.get(renderPointIndex - 1));
//            Vector2f point2 = new Vector2f(renderPoints.get(renderPointIndex));
//            Vector2f delta = Vector2f.sub(point2, point1, null);
//            float angle = VectorUtils.getFacing(delta);
//            angles.add((double) angle);
//        }
//        if (renderPointIndex + 1 < renderPoints.size()) { //if next exists
//            Vector2f point1 = new Vector2f(renderPoints.get(renderPointIndex));
//            Vector2f point2 = new Vector2f(renderPoints.get(renderPointIndex + 1));
//            Vector2f delta = Vector2f.sub(point2, point1, null);
//            float angle = VectorUtils.getFacing(delta);
//            angles.add((double) angle);
//        }
//
//        double[] d = new double[angles.size()];
//        for (int j = 0; j < angles.size(); j++) {
//            d[j] = angles.get(j);
//        }
//        float meanAngle = (float) getMeanAngle(d);

//        int amount = (int) (255 * Math.sin(Math.toRadians(meanAngle * 10f)));
//        amount = amount / 5;
        int red = baseColor.getRed() ;//+ amount;
        red = Math.max(0,Math.min(red, 255));
        int green = baseColor.getGreen() ;//+ amount;
        green = Math.max(0,Math.min(green, 255));
        int blue = baseColor.getBlue() ;//+ amount;
        blue = Math.max(0,Math.min(blue, 255));
        return new Color(red, green, blue, (int) Math.max(0, Math.min(fadeLevel * 255f, 255)));

    }

    public void setBaseColor(Color baseColor) {
        this.baseColor = baseColor;
    }

    public void kill() {
        deadAndFading = true;
        this.target = null;
    }
}
