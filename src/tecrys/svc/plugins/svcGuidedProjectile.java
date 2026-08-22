package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class svcGuidedProjectile extends BaseEveryFrameCombatPlugin {

    private static final String GUIDANCE_MODE_PRIMARY = "INTERCEPT_SWARM";
    private static final String GUIDANCE_MODE_SECONDARY = "REACQUIRE_RANDOM_PROJ";
    private static final List<String> VALID_TARGET_TYPES = new ArrayList<>();

    static {
        VALID_TARGET_TYPES.add("MISSILE");
        VALID_TARGET_TYPES.add("FIGHTER");
    }

    private static final float TARGET_REACQUIRE_RANGE = 300f;
    private static final float TARGET_REACQUIRE_ANGLE = 135f;
    private static final float TURN_RATE = 480f;
    private static final float SWAY_AMOUNT_PRIMARY = 60f;
    private static final float SWAY_AMOUNT_SECONDARY = 30f;
    private static final float SWAY_PERIOD_PRIMARY = 2f;
    private static final float SWAY_PERIOD_SECONDARY = 1f;
    private static final float SWAY_FALLOFF_FACTOR = 0f;
    private static final float ONE_TURN_DUMB_INACCURACY = 0f;
    private static final float ONE_TURN_TARGET_INACCURACY = 0f;
    private static final int INTERCEPT_ITERATIONS = 4;
    private static final float INTERCEPT_ACCURACY_FACTOR = 1f;
    private static final float GUIDANCE_DELAY_MAX = 0f;
    private static final float GUIDANCE_DELAY_MIN = 0f;
    private static final boolean BROKEN_BY_PHASE = true;
    private static final boolean RETARGET_ON_SIDE_SWITCH = true;

    private DamagingProjectileAPI proj;
    private CombatEntityAPI target;
    private Vector2f targetPoint;
    private float targetAngle;
    private float swayCounter1;
    private float swayCounter2;
    private float lifeCounter;
    private float estimateMaxLife;
    private float delayCounter;
    private Vector2f offsetVelocity;
    private Vector2f lastTargetPos;
    private float actualGuidanceDelay;

    public svcGuidedProjectile(@NotNull DamagingProjectileAPI proj, CombatEntityAPI target) {
        this.proj = proj;
        this.target = target;
        lastTargetPos = target != null ? target.getLocation() : new Vector2f(proj.getLocation());
        swayCounter1 = MathUtils.getRandomNumberInRange(0f, 1f);
        swayCounter2 = MathUtils.getRandomNumberInRange(0f, 1f);
        lifeCounter = 0f;

        Vector2f relVel = new Vector2f(proj.getVelocity().x - proj.getSource().getVelocity().x, proj.getVelocity().y - proj.getSource().getVelocity().y);
        estimateMaxLife = proj.getWeapon().getRange() / Math.max(0.1f, relVel.length());

        delayCounter = 0f;
        actualGuidanceDelay = MathUtils.getRandomNumberInRange(GUIDANCE_DELAY_MIN, GUIDANCE_DELAY_MAX);

        if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_DUMB")) {
            targetAngle = proj.getWeapon().getCurrAngle() + MathUtils.getRandomNumberInRange(-ONE_TURN_DUMB_INACCURACY, ONE_TURN_DUMB_INACCURACY);
            offsetVelocity = new Vector2f(proj.getSource().getVelocity());
        } else if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_TARGET")) {
            targetPoint = MathUtils.getRandomPointInCircle(getApproximateInterception(25), ONE_TURN_TARGET_INACCURACY);
        } else if (GUIDANCE_MODE_PRIMARY.contains("SWARM") && target != null) {
            applySwarmOffset();
        } else {
            targetPoint = new Vector2f();
        }
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        if (engine.isPaused()) amount = 0f;

        if (proj == null || proj.didDamage() || proj.isFading() || !engine.isEntityInPlay(proj)) {
            engine.removePlugin(this);
            return;
        }

        lifeCounter = Math.min(lifeCounter + amount, estimateMaxLife);

        if (delayCounter < actualGuidanceDelay) {
            delayCounter += amount;
            return;
        }

        swayCounter1 += amount * SWAY_PERIOD_PRIMARY;
        swayCounter2 += amount * SWAY_PERIOD_SECONDARY;

        float swayThisFrame = (float) Math.pow(1f - (lifeCounter / estimateMaxLife), SWAY_FALLOFF_FACTOR) *
                ((float) (FastTrig.sin(Math.PI * 2f * swayCounter1) * SWAY_AMOUNT_PRIMARY) + (float) (FastTrig.sin(Math.PI * 2f * swayCounter2) * SWAY_AMOUNT_SECONDARY));

        if (!GUIDANCE_MODE_PRIMARY.contains("ONE_TURN")) {
            if (target != null) {
                if (!engine.isEntityInPlay(target)) {
                    target = null;
                } else if (target instanceof ShipAPI shipTarget) {
                    if (shipTarget.isHulk() || (shipTarget.isPhased() && BROKEN_BY_PHASE) || (target.getOwner() == proj.getOwner() && RETARGET_ON_SIDE_SWITCH)) {
                        target = null;
                    }
                }
            }

            if (target == null) {
                if (GUIDANCE_MODE_SECONDARY.equals("NONE")) {
                    engine.removePlugin(this);
                    return;
                } else if (GUIDANCE_MODE_SECONDARY.equals("DISAPPEAR")) {
                    engine.removeEntity(proj);
                    engine.removePlugin(this);
                    return;
                } else {
                    reacquireTarget();
                }
            } else {
                lastTargetPos.set(target.getLocation());
            }
        }

        if (!GUIDANCE_MODE_PRIMARY.contains("ONE_TURN") && target == null) return;

        float currentVelocityLen = proj.getVelocity().length();
        float facingSwayless = proj.getFacing() - swayThisFrame;
        float targetAngleToHit = 0f;

        if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_DUMB")) {
            targetAngleToHit = targetAngle;
            float turnDir = Misc.getClosestTurnDirection(facingSwayless, targetAngleToHit);
            float angleDiffAbsolute = Math.abs(MathUtils.getShortestRotation(facingSwayless, targetAngleToHit));
            facingSwayless += turnDir * Math.min(angleDiffAbsolute, TURN_RATE * amount);

            proj.setFacing(facingSwayless + swayThisFrame);
            float rads = (float) Math.toRadians(facingSwayless + swayThisFrame);

            float pureLen = new Vector2f(proj.getVelocity().x - offsetVelocity.x, proj.getVelocity().y - offsetVelocity.y).length();
            proj.getVelocity().set((float) FastTrig.cos(rads) * pureLen + offsetVelocity.x, (float) FastTrig.sin(rads) * pureLen + offsetVelocity.y);
            return;
        } else if (GUIDANCE_MODE_PRIMARY.equals("ONE_TURN_TARGET")) {
            targetAngleToHit = VectorUtils.getAngle(proj.getLocation(), targetPoint);
        } else if (GUIDANCE_MODE_PRIMARY.contains("DUMBCHASER")) {
            Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
            Vector2f.add(target.getLocation(), targetPointRotated, targetPointRotated);
            targetAngleToHit = VectorUtils.getAngle(proj.getLocation(), targetPointRotated);
        } else if (GUIDANCE_MODE_PRIMARY.contains("INTERCEPT")) {
            Vector2f targetPointRotated = VectorUtils.rotate(new Vector2f(targetPoint), target.getFacing());
            Vector2f.add(getApproximateInterception(INTERCEPT_ITERATIONS), targetPointRotated, targetPointRotated);
            targetAngleToHit = VectorUtils.getAngle(proj.getLocation(), targetPointRotated);
        }

        float turnDir = Misc.getClosestTurnDirection(facingSwayless, targetAngleToHit);
        float angleDiffAbsolute = Math.abs(MathUtils.getShortestRotation(facingSwayless, targetAngleToHit));
        facingSwayless += turnDir * Math.min(angleDiffAbsolute, TURN_RATE * amount);

        proj.setFacing(facingSwayless + swayThisFrame);
        float finalRad = (float) Math.toRadians(facingSwayless + swayThisFrame);
        proj.getVelocity().set((float) FastTrig.cos(finalRad) * currentVelocityLen, (float) FastTrig.sin(finalRad) * currentVelocityLen);
    }

    private void reacquireTarget() {
        CombatEntityAPI newTarget = null;
        Vector2f centerOfDetection = GUIDANCE_MODE_SECONDARY.contains("_PROJ") ? proj.getLocation() : lastTargetPos;
        List<CombatEntityAPI> potentialTargets = new ArrayList<>();

        if (VALID_TARGET_TYPES.contains("ASTEROID")) {
            for (CombatEntityAPI potTarget : CombatUtils.getAsteroidsWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
                if (potTarget.getOwner() != proj.getOwner() && Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()))) < TARGET_REACQUIRE_ANGLE) {
                    potentialTargets.add(potTarget);
                }
            }
        }
        if (VALID_TARGET_TYPES.contains("MISSILE")) {
            for (CombatEntityAPI potTarget : CombatUtils.getMissilesWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
                if (potTarget.getOwner() != proj.getOwner() && Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()))) < TARGET_REACQUIRE_ANGLE) {
                    potentialTargets.add(potTarget);
                }
            }
        }

        for (ShipAPI potTarget : CombatUtils.getShipsWithinRange(centerOfDetection, TARGET_REACQUIRE_RANGE)) {
            if (potTarget.getOwner() == proj.getOwner() || potTarget.isHulk() || (potTarget.isPhased() && BROKEN_BY_PHASE)) continue;
            if (Math.abs(MathUtils.getShortestRotation(proj.getFacing(), VectorUtils.getAngle(proj.getLocation(), potTarget.getLocation()))) > TARGET_REACQUIRE_ANGLE) continue;

            String sizeStr = potTarget.getHullSize().toString();
            if ((sizeStr.equals("FIGHTER") && VALID_TARGET_TYPES.contains("FIGHTER")) ||
                    (sizeStr.equals("FRIGATE") && VALID_TARGET_TYPES.contains("FRIGATE")) ||
                    (sizeStr.equals("DESTROYER") && VALID_TARGET_TYPES.contains("DESTROYER")) ||
                    (sizeStr.equals("CRUISER") && VALID_TARGET_TYPES.contains("CRUISER")) ||
                    (sizeStr.equals("CAPITAL_SHIP") && VALID_TARGET_TYPES.contains("CAPITAL"))) {
                potentialTargets.add(potTarget);
            }
        }

        if (!potentialTargets.isEmpty()) {
            if (GUIDANCE_MODE_SECONDARY.contains("REACQUIRE_NEAREST")) {
                for (CombatEntityAPI potTarget : potentialTargets) {
                    if (newTarget == null || MathUtils.getDistance(newTarget, centerOfDetection) > MathUtils.getDistance(potTarget, centerOfDetection)) {
                        newTarget = potTarget;
                    }
                }
            } else if (GUIDANCE_MODE_SECONDARY.contains("REACQUIRE_RANDOM")) {
                newTarget = potentialTargets.get(MathUtils.getRandomNumberInRange(0, potentialTargets.size() - 1));
            }

            target = newTarget;
            if (GUIDANCE_MODE_PRIMARY.contains("SWARM")) {
                applySwarmOffset();
            }
        }
    }

    private Vector2f getApproximateInterception(int calculationSteps) {
        Vector2f returnPoint = new Vector2f(target.getLocation());
        float projVelLen = Math.max(1f, proj.getVelocity().length());

        for (int i = 0; i < calculationSteps; i++) {
            float arrivalTime = MathUtils.getDistance(proj.getLocation(), returnPoint) / projVelLen;
            returnPoint.set(target.getLocation().x + (target.getVelocity().x * arrivalTime * INTERCEPT_ACCURACY_FACTOR),
                    target.getLocation().y + (target.getVelocity().y * arrivalTime * INTERCEPT_ACCURACY_FACTOR));
        }
        return returnPoint;
    }

    private void applySwarmOffset() {
        int i = 40;
        boolean success = false;
        while (i > 0 && target != null) {
            i--;
            Vector2f potPoint = MathUtils.getRandomPointInCircle(target.getLocation(), target.getCollisionRadius());
            if (CollisionUtils.isPointWithinBounds(potPoint, target)) {
                Vector2f.sub(potPoint, target.getLocation(), potPoint);
                targetPoint = VectorUtils.rotate(potPoint, -target.getFacing());
                success = true;
                break;
            }
        }
        if (!success) {
            targetPoint = new Vector2f();
        }
    }
}