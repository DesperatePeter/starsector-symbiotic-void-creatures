package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
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

import java.awt.Color;
import java.util.List;

public class GrapplerRopePlugin extends svcBaseKinematicRopePlugin {
    protected WeaponAPI attachedWeapon;
    protected DamagingProjectileAPI attachedProj;
    private CombatEntityAPI target;
    private Vector2f offset;
    private float impactFacing;
    private Color baseColor;
    private boolean deadAndFading = false;
    private float fadeLevel = 1.0f;

    private final Vector2f scratchVector = new Vector2f();

    public GrapplerRopePlugin(int pluginID, int elementCount, float trailWidth, boolean is_pd, WeaponAPI attachedWeapon, DamagingProjectileAPI attachedProj) {
        super(pluginID, elementCount, trailWidth, is_pd);
        this.attachedWeapon = attachedWeapon;
        this.attachedProj = attachedProj;
        this.target = null;
        this.is_pd = is_pd;

        for (int i = 0; i < elementCount; i++) {
            SegmentPoint point = new SegmentPoint();
            point.location.set(MathUtils.getRandomPointInCircle(attachedWeapon.getLocation(), 5f));
            segmentPoints.add(point);
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || attachedProj == null) return;

        if (attachedProj.isExpired() || attachedProj.isFading() || !attachedWeapon.getShip().isAlive() || (target != null && (target.getHullLevel() <= 0f || target.isExpired()))) {
            this.kill();
        }

        if (deadAndFading) {
            fadeLevel -= amount * 5f;
            if (fadeLevel <= 0.0f) {
                engine.removePlugin(this);
                return;
            }
        }

        if (target != null) {
            ropeParams.segmentGoalLength = Math.max(3f, ropeParams.segmentGoalLength * 0.99f);
            SpriteAPI sprite = Global.getSettings().getSprite("graphics/missiles/breach_srm.png");

            scratchVector.set(offset);
            VectorUtils.rotate(scratchVector, target.getFacing(), scratchVector);
            Vector2f.add(scratchVector, target.getLocation(), scratchVector);

            MagicRender.singleframe(sprite, scratchVector, new Vector2f(sprite.getWidth(), sprite.getHeight()), impactFacing + target.getFacing() - 90f, Color.white, false);
        }

        segmentPoints.get(0).getLocation().set(attachedWeapon.getLocation());
        segmentPoints.get(0).getVelocity().set(attachedWeapon.getShip().getVelocity());

        if (!deadAndFading) {
            SegmentPoint lastPoint = segmentPoints.get(segmentPoints.size() - 1);
            if (target == null) {
                lastPoint.getLocation().set(attachedProj.getLocation());
                lastPoint.getVelocity().set(attachedProj.getVelocity());
            } else {
                scratchVector.set(offset);
                VectorUtils.rotate(scratchVector, target.getFacing(), scratchVector);
                Vector2f.add(target.getLocation(), scratchVector, scratchVector);
                lastPoint.getLocation().set(scratchVector);
                lastPoint.getVelocity().set(target.getVelocity());
            }
        }

        super.advance(amount, events);

        if (target != null) {
            applyRopeForces();
        }
    }

    private void applyRopeForces() {
        Vector2f force = new Vector2f(segmentPoints.get(segmentPoints.size() - 1).accel);
        force.scale(1f / Math.max(1f, target.getMass()));
        force.scale(Misc.getDistance(attachedWeapon.getLocation(), target.getLocation()) / (ropeParams.segmentGoalLength * elementCount));

        Vector2f off = new Vector2f(offset);
        VectorUtils.rotate(off, target.getFacing(), off);
        force = VectorUtils.rotate(force, -VectorUtils.getFacing(off));

        Vector2f fr = new Vector2f(force.x, 0f);
        Vector2f ft = new Vector2f(0f, force.y);
        VectorUtils.rotate(fr, VectorUtils.getFacing(off), fr);

        Vector2f.add(target.getVelocity(), fr, target.getVelocity());
        target.setAngularVelocity(target.getAngularVelocity() + ft.y);

        Vector2f force2 = new Vector2f(segmentPoints.get(0).accel);
        force2.scale(1f / Math.max(1f, attachedWeapon.getShip().getMass()));
        force2.scale(Misc.getDistance(attachedWeapon.getLocation(), target.getLocation()) / (ropeParams.segmentGoalLength * elementCount));

        Vector2f off2 = new Vector2f(attachedWeapon.getSlot().getLocation());
        VectorUtils.rotate(off2, attachedWeapon.getShip().getFacing(), off2);
        force2 = VectorUtils.rotate(force2, -VectorUtils.getFacing(off2));

        Vector2f fr2 = new Vector2f(force2.x, 0f);
        Vector2f ft2 = new Vector2f(0f, force2.y);
        VectorUtils.rotate(fr2, VectorUtils.getFacing(off2), fr2);

        fr2.scale(0.5f);
        ft2.scale(0.5f);

        Vector2f.add(attachedWeapon.getShip().getVelocity(), fr2, attachedWeapon.getShip().getVelocity());
        attachedWeapon.getShip().setAngularVelocity(attachedWeapon.getShip().getAngularVelocity() + ft2.y);
    }

    public void attach(CombatEntityAPI target, Vector2f point, float projFacing) {
        this.target = target;
        Vector2f o = Vector2f.sub(point, target.getLocation(), null);
        this.offset = VectorUtils.rotate(o, -target.getFacing());
        this.impactFacing = projFacing - target.getFacing();
    }

    @Override
    protected void computeStringPhysics(float amount) {
        super.computeStringPhysics(amount);
        for (SegmentPoint segmentPoint : segmentPoints) {
            Vector2f noise = new Vector2f(MathUtils.getRandomNumberInRange(0f, 5f), 0f);
            VectorUtils.rotate(noise, MathUtils.getRandomNumberInRange(0f, 360f), noise);
            Vector2f.add(segmentPoint.getVelocity(), noise, segmentPoint.getVelocity());
        }
    }

    @Override
    protected Color computeColorForSegment(int renderPointIndex) {
        int red = Math.max(0, Math.min(baseColor.getRed(), 255));
        int green = Math.max(0, Math.min(baseColor.getGreen(), 255));
        int blue = Math.max(0, Math.min(baseColor.getBlue(), 255));
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