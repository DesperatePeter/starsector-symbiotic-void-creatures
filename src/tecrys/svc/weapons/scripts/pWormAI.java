package tecrys.svc.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;

public class pWormAI implements MissileAIPlugin, GuidedMissileAI {

    // [KEEP ALL YOUR EXISTING SETTINGS/VARIABLES HERE]
    private final float OVERSHOT_ANGLE = 10;
    private final float WAVE_TIME = 5;
    private final float WAVE_AMPLITUDE = 900;
    private final float DAMPING = 0.1f;
    private final boolean OVERSTEER = false;
    private final boolean TARGET_SWITCH = false;
    private final MagicTargeting.targetSeeking seeking = MagicTargeting.targetSeeking.LOCAL_RANDOM;
    private final int fighters = 1;
    private final int frigates = 3;
    private final int destroyers = 2;
    private final int cruisers = 2;
    private final int capitals = 2;
    private final int SEARCH_CONE = 70;
    private final int MAX_SEARCH_RANGE = 2000;
    private final boolean FAILSAFE = true;
    private float PRECISION_RANGE = 500;
    private final boolean LEADING = false;
    private float ECCM = 1;

    private final float MAX_SPEED;
    private final float OFFSET;
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch = true;
    private float timer = 0, check = 1f;

    // NEW: Add a cooldown for target searching
    private float searchTimer = 0f;

    public pWormAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")) {
            ECCM = 1;
        }
        PRECISION_RANGE = (float) Math.pow((2 * PRECISION_RANGE), 2);
        OFFSET = (float) (Math.random() * MathUtils.FPI * 2);
    }

    @Override
    public void advance(float amount) {
        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        if (engine.isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {
            return;
        }

        // Optimized target switching with a 0.5s cooldown
        if (target == null || (TARGET_SWITCH && ((target instanceof ShipAPI && !((ShipAPI) target).isAlive()) || !engine.isEntityInPlay(target)))) {
            searchTimer -= amount;
            if (searchTimer <= 0f) {
                setTarget(MagicTargeting.pickTarget(
                        MISSILE, seeking, MAX_SEARCH_RANGE, SEARCH_CONE,
                        fighters, frigates, destroyers, cruisers, capitals, FAILSAFE
                ));
                // Only scan for targets twice a second if we can't find one
                searchTimer = 0.5f;
            }
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        timer += amount;

        if (launch || timer >= check) {
            launch = false;
            timer -= check;
            check = Math.min(0.25f, Math.max(0.05f, MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) / PRECISION_RANGE));

            if (LEADING) {
                lead = AIUtils.getBestInterceptPoint(
                        MISSILE.getLocation(),
                        MAX_SPEED * ECCM,
                        target.getLocation(),
                        target.getVelocity()
                );
                if (lead == null) {
                    lead = target.getLocation();
                }
            } else {
                lead = target.getLocation();
            }
        }

        float correctAngle = VectorUtils.getAngle(MISSILE.getLocation(), lead);

        if (OVERSTEER) {
            float offCourseAngle = MathUtils.getShortestRotation(VectorUtils.getFacing(MISSILE.getVelocity()), correctAngle);
            float correction = MathUtils.getShortestRotation(correctAngle, VectorUtils.getFacing(MISSILE.getVelocity()) + 180)
                    * 0.5f * (float) ((FastTrig.sin(MathUtils.FPI / 90 * (Math.min(Math.abs(offCourseAngle), 45)))));
            correctAngle = correctAngle + correction;
        }

        if (WAVE_AMPLITUDE > 0) {
            float multiplier = 1;
            if (ECCM <= 1) {
                multiplier = 0.3f;
            }
            correctAngle += multiplier * WAVE_AMPLITUDE * check * Math.cos(OFFSET + MISSILE.getElapsed() * (2 * MathUtils.FPI / WAVE_TIME));
        }

        float aimAngle = MathUtils.getShortestRotation(MISSILE.getFacing(), correctAngle);

        if (OVERSHOT_ANGLE <= 0 || Math.abs(aimAngle) < OVERSHOT_ANGLE) {
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
        }

        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }

        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public void init(CombatEngineAPI engine) {}
}