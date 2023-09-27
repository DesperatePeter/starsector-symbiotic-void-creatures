//By Tartiflette, fast and highly customizable Missile AI.
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

    //////////////////////
    //     SETTINGS     //
    //////////////////////

    //Angle with the target beyond which the missile turn around without accelerating. Avoid endless circling.
    //  Set to a negative value to disable
    private final float OVERSHOT_ANGLE = 10;

    //Time to complete a wave in seconds.
    private final float WAVE_TIME = 5;

    //Max angle of the waving in degree (divided by 3 with ECCM). Set to a negative value to avoid all waving.
    private final float WAVE_AMPLITUDE = 900;

    //Damping of the turn speed when closing on the desired aim. The smaller the snappier.
    private final float DAMPING = 0.1f;

    //Does the missile try to correct it's velocity vector as fast as possible or just point to the desired direction and drift a bit?
    //  Can create strange results with large waving
    //  Require a projectile with a decent turn rate and around twice that in turn acceleration
    //  Usefull for slow torpedoes with low forward acceleration, or ultra precise anti-fighter missiles.     
    private final boolean OVERSTEER = false;  //REQUIRE NO OVERSHOOT ANGLE!

    //Does the missile switch its target if it has been destroyed?
    private final boolean TARGET_SWITCH = true;

    //Does the missile find a random target or aways tries to hit the ship's one?    
    /*
     *  NO_RANDOM,
     * If the launching ship has a valid target within arc, the missile will pursue it.
     * If there is no target, it will check for an unselected cursor target within arc.
     * If there is none, it will pursue its closest valid threat within arc.
     *
     *  LOCAL_RANDOM,
     * If the ship has a target, the missile will pick a random valid threat around that one.
     * If the ship has none, the missile will pursue a random valid threat around the cursor, or itself.
     * Can produce strange behavior if used with a limited search cone.
     *
     *  FULL_RANDOM,
     * The missile will always seek a random valid threat within arc around itself.
     *
     *  IGNORE_SOURCE,
     * The missile will pick the closest target of interest. Useful for custom MIRVs.
     *
     */
    private final MagicTargeting.targetSeeking seeking = MagicTargeting.targetSeeking.LOCAL_RANDOM;

    //Target class priorities
    //set to 0 to ignore that class
    private final int fighters = 1;
    private final int frigates = 3;
    private final int destroyers = 2;
    private final int cruisers = 2;
    private final int capitals = 2;

    //Arc to look for targets into
    //set to 360 or more to ignore
    private final int SEARCH_CONE = 270;

    //range in which the missile seek a target in game units.
    private final int MAX_SEARCH_RANGE = 2000;

    //should the missile fall back to the closest enemy when no target is found within the search parameters
    //only used with limited search cones
    private final boolean FAILSAFE = true;

    //range under which the missile start to get progressively more precise in game units.
    private float PRECISION_RANGE = 500;

    //Is the missile lead the target or tailchase it?
    private final boolean LEADING = true;

    //Leading loss without ECCM hullmod. The higher, the less accurate the leading calculation will be.
    //   1: perfect leading with and without ECCM
    //   2: half precision without ECCM
    //   3: a third as precise without ECCM. Default
    //   4, 5, 6 etc : 1/4th, 1/5th, 1/6th etc precision.
    private float ECCM = 1;   //A VALUE BELOW 1 WILL PREVENT THE MISSILE FROM EVER HITTING ITS TARGET!


    //////////////////////
    //    VARIABLES     //
    //////////////////////

    //max speed of the missile after modifiers.
    private final float MAX_SPEED;
    //Random starting offset for the waving.
    private final float OFFSET;
    private CombatEngineAPI engine;
    private final MissileAPI MISSILE;
    private CombatEntityAPI target;
    private Vector2f lead = new Vector2f();
    private boolean launch = true;
    private float timer = 0, check = 0f;

    //////////////////////
    //  DATA COLLECTING //
    //////////////////////

    public pWormAI(MissileAPI missile, ShipAPI launchingShip) {
        this.MISSILE = missile;
        MAX_SPEED = missile.getMaxSpeed();
        if (missile.getSource().getVariant().getHullMods().contains("eccm")) {
            ECCM = 1;
        }
        //calculate the precision range factor
        PRECISION_RANGE = (float) Math.pow((2 * PRECISION_RANGE), 2);
        OFFSET = (float) (Math.random() * MathUtils.FPI * 2);
    }

    //////////////////////
    //   MAIN AI LOOP   //
    //////////////////////

    @Override
    public void advance(float amount) {

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }

        //skip the AI if the game is paused, the missile is engineless or fading
        if (Global.getCombatEngine().isPaused() || MISSILE.isFading() || MISSILE.isFizzling()) {
            return;
        }

        //assigning a target if there is none or it got destroyed
        if (target == null
                || (TARGET_SWITCH
                && ((target instanceof ShipAPI && !((ShipAPI) target).isAlive())
                || !engine.isEntityInPlay(target))
        )
        ) {
            setTarget(
                    MagicTargeting.pickTarget(
                            MISSILE,
                            seeking,
                            MAX_SEARCH_RANGE,
                            SEARCH_CONE,
                            fighters,
                            frigates,
                            destroyers,
                            cruisers,
                            capitals,
                            FAILSAFE
                    )
            );
            //forced acceleration by default
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        timer += amount;
        //finding lead point to aim to        
        if (launch || timer >= check) {
            launch = false;
            timer -= check;
            //set the next check time
            check = Math.min(
                    0.25f,
                    Math.max(
                            0.05f,
                            MathUtils.getDistanceSquared(MISSILE.getLocation(), target.getLocation()) / PRECISION_RANGE)
            );
            if (LEADING) {
                //best intercepting point
                lead = AIUtils.getBestInterceptPoint(
                        MISSILE.getLocation(),
                        MAX_SPEED * ECCM, //if eccm is intalled the point is accurate, otherwise it's placed closer to the target (almost tailchasing)
                        target.getLocation(),
                        target.getVelocity()
                );
                //null pointer protection
                if (lead == null) {
                    lead = target.getLocation();
                }
            } else {
                lead = target.getLocation();
            }
        }

        //best velocity vector angle for interception
        float correctAngle = VectorUtils.getAngle(
                MISSILE.getLocation(),
                lead
        );

        if (OVERSTEER) {
            //velocity angle correction
            float offCourseAngle = MathUtils.getShortestRotation(
                    VectorUtils.getFacing(MISSILE.getVelocity()),
                    correctAngle
            );

            float correction = MathUtils.getShortestRotation(
                    correctAngle,
                    VectorUtils.getFacing(MISSILE.getVelocity()) + 180
            )
                    * 0.5f * //oversteer
                    (float) ((FastTrig.sin(MathUtils.FPI / 90 * (Math.min(Math.abs(offCourseAngle), 45))))); //damping when the correction isn't important

            //modified optimal facing to correct the velocity vector angle as soon as possible
            correctAngle = correctAngle + correction;
        }

        if (WAVE_AMPLITUDE > 0) {
            //waving
            float multiplier = 1;
            if (ECCM <= 1) {
                multiplier = 0.3f;
            }
            correctAngle += multiplier * WAVE_AMPLITUDE * check * Math.cos(OFFSET + MISSILE.getElapsed() * (2 * MathUtils.FPI / WAVE_TIME));
        }

        //target angle for interception        
        float aimAngle = MathUtils.getShortestRotation(MISSILE.getFacing(), correctAngle);

        if (OVERSHOT_ANGLE <= 0 || Math.abs(aimAngle) < OVERSHOT_ANGLE) {
            MISSILE.giveCommand(ShipCommand.ACCELERATE);
        }

        if (aimAngle < 0) {
            MISSILE.giveCommand(ShipCommand.TURN_RIGHT);
        } else {
            MISSILE.giveCommand(ShipCommand.TURN_LEFT);
        }

        // Damp angular velocity if the missile aim is getting close to the targeted angle
        if (Math.abs(aimAngle) < Math.abs(MISSILE.getAngularVelocity()) * DAMPING) {
            MISSILE.setAngularVelocity(aimAngle / DAMPING);
        }
    }

    //////////////////////
    //    TARGETING     //
    //////////////////////

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
    }

    public void init(CombatEngineAPI engine) {
    }
}