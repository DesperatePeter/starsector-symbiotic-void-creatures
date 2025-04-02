package tecrys.svc.shipsystems.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.RiftLanceEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VoidlingShroud extends RoilingSwarmEffect {

    public static Color SHROUD_COLOR = new Color(170, 100, 25, 255);
    public static Color SHROUD_GLOW_COLOR = new Color(130, 80, 30, 255);

    public static interface VoidlingShroudEffectFilter {
        boolean isParticleOk(VoidlingShroud shroud, Vector2f loc);
    }

    public static class VoidlingShroudParams extends RoilingSwarmParams {


        public float negativeParticleGenRate = 1f;
        public float negativeParticleSizeMult = 1f;
        public float negativeParticleVelMult = 1f;
        public float negativeParticleDurMult = 1f;
        public float negativeParticleSpeedCapMult = 1.5f;
        public float negativeParticleSpeedCap = 10000f;
        public float negativeParticleAreaMult = 1f;
        public float negativeParticleClearCenterAreaRadius = 0f;
        public boolean negativeParticleHighContrastMode = false;
        public int negativeParticleNumBase = 11;
        public int negativeParticleAlphaIntOverride = -1;
        public Color negativeParticleColorOverride = null;
    }


    public static VoidlingShroud getShroudFor(CombatEntityAPI entity) {
        RoilingSwarmEffect swarm = RoilingSwarmEffect.getSwarmFor(entity);
        if (swarm instanceof VoidlingShroud) {
            return (VoidlingShroud) swarm;
        }
        return null;
    }

    public static VoidlingShroudParams createBaselineParams(CombatEntityAPI attachedTo) {
        if (!(attachedTo instanceof ShipAPI)) {
            return null;
        }

        ShipAPI ship = (ShipAPI) attachedTo;
        VoidlingShroudParams params = new VoidlingShroudParams();
        float radius;
        int numMembers;

        params.spriteCat = "misc";
        params.spriteKey = "nebula_particles";

        params.despawnSound = null; // no free-flying swarms, all are ships that have an explosion sound

        params.baseDur = 1f;
        params.durRange = 2f;
        params.memberRespawnRate = 100f;

        params.memberExchangeClass = null;
        params.flockingClass = null;
        params.maxSpeed = ship.getMaxSpeed()*3f +
                Math.max(ship.getMaxSpeed()*3f * 0.25f + 50f, 100f);

        params.baseSpriteSize = 64f;
        params.maxTurnRate = 120f;

        numMembers = 100;
        radius = ship.getCollisionRadius()/2f;

//		radius = 100;
//		numMembers = 40;

        params.flashCoreRadiusMult = 0f;
        //params.flashRadius = 0f;
        params.flashRadius = 300f;
        params.flashRadius = 150f;
        params.renderFlashOnSameLayer = true;
        params.flashRateMult = 0.25f;
        //params.flashFrequency = 10f;
        //params.flashFrequency = 20f;
        //params.flashFrequency = 40f;
        params.flashFrequency = 17f;
        params.numToFlash = 2;
        //params.flashFrequency = 50f;
        params.flashProbability = 1f;

        params.swarmLeadsByFractionOfVelocity = 0.33f;

        params.alphaMult = 1f;
        params.alphaMultBase = 1f;
        params.alphaMultFlash = 1f;

//		params.alphaMult = 0.25f;
//		params.negativeParticleGenRate = 0f;

        //params.color = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
        params.color = SHROUD_COLOR;
        params.flashFringeColor = SHROUD_GLOW_COLOR;

//		params.color = new Color(121, 56, 171, 255);
//		params.color = Misc.setBrightness(params.color, 200);
//		//params.flashFringeColor = new Color(7, 163, 169, 255);
//		params.flashFringeColor = params.color;
//		params.flashFringeColor = Misc.setBrightness(params.flashFringeColor, 250);

        params.flashCoreColor = Misc.setBrightness(params.color, 255);


        //params.despawnDist = params.maxOffset + 300f;

        params.maxOffset = radius;
        params.initialMembers = numMembers;
        params.baseMembersToMaintain = params.initialMembers;

        return params;
    }




    protected IntervalUtil interval = new IntervalUtil(0.075f, 0.125f);
    protected IntervalUtil overloadInterval = new IntervalUtil(0.075f, 0.125f);
    //protected IntervalUtil ventingInterval = new IntervalUtil(0.075f, 0.125f);
    protected ShipAPI ship;
    protected VoidlingShroudParams shroudParams;


    public VoidlingShroud(CombatEntityAPI attachedTo) {
        this(attachedTo, createBaselineParams(attachedTo));
    }
    public VoidlingShroud(CombatEntityAPI attachedTo, VoidlingShroudParams params) {
        super(attachedTo, params);
        this.shroudParams = params;
        if (attachedTo instanceof ShipAPI) {
            ship = (ShipAPI) attachedTo;
        }
    }



    @Override
    public int getNumMembersToMaintain() {
        return super.getNumMembersToMaintain();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);


//		overloaded = true;
//		overloaded = !ship.getShield().isOn();


        params.springStretchMult = 10f;
        params.flashProbability = 1f;
        params.despawnDist = 0f;
        //params.alphaMult = 1f;



//		ventingInterval.advance(amount * 1f);
//		if (ventingInterval.intervalElapsed() && ship != null && venting) {
//
//		}
        if (ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.IDLE){
            params.alphaMult = 0f;
            params.alphaMultBase = 0f;
            params.alphaMultFlash = 0f;

            return;
        } else {
            params.alphaMult = 1f;
            params.alphaMultBase = 1f;
            params.alphaMultFlash = 1f;
        }


        interval.advance(amount * shroudParams.negativeParticleGenRate);
        if (interval.intervalElapsed()) {
            CombatEngineAPI engine = Global.getCombatEngine();
            boolean smallerDark = false;
            //smallerDark = true;
            Color c = RiftLanceEffect.getColorForDarkening(params.color);
            c = Misc.setAlpha(c, 100);
            int num = shroudParams.negativeParticleNumBase;
            if (shroudParams.negativeParticleHighContrastMode) {
                c = Misc.setAlpha(c, 150);
            }
            if (shroudParams.negativeParticleColorOverride != null) {
                c = shroudParams.negativeParticleColorOverride;
            }
            if (shroudParams.negativeParticleAlphaIntOverride >= 0) {
                c = Misc.setAlpha(c, shroudParams.negativeParticleAlphaIntOverride);
            }

            float baseDuration = 2f;
            Vector2f vel = new Vector2f(attachedTo.getVelocity());
            float speed = vel.length();
            if (attachedTo instanceof ShipAPI) {
                float maxSpeed = ((ShipAPI)attachedTo).getMaxSpeed() * shroudParams.negativeParticleSpeedCapMult;
                maxSpeed = Math.min(maxSpeed, shroudParams.negativeParticleSpeedCap);
                if (speed > maxSpeed && speed > 1f) {
                    vel.scale(maxSpeed / speed);
                }
            }

            float baseSize = params.maxOffset * 2f;
            //baseSize = params.maxOffset * 1f;

            //float size = ship.getCollisionRadius() * 0.35f;
            float size = baseSize * 0.33f;

            float extraDur = 0f;

            // so that switching the view to another ship near a dweller part
            // doesn't result in it not having negative particles
            Global.getCombatEngine().getViewport().setEverythingNearViewport(true);

            //for (int i = 0; i < 3; i++) {
            for (int i = 0; i < num; i++) {
                //for (int i = 0; i < 7; i++) {
                Vector2f point = new Vector2f(attachedTo.getLocation());
                float min = shroudParams.negativeParticleClearCenterAreaRadius;
                if (min > 0) {
                    point = Misc.getPointWithinRadiusUniform(point, min,
                            Math.max(min, baseSize * 0.75f * (smallerDark ? 0.85f : 1f) * shroudParams.negativeParticleAreaMult), Misc.random);
                } else {
                    point = Misc.getPointWithinRadiusUniform(point,
                            baseSize * 0.75f * (smallerDark ? 0.85f : 1f) * shroudParams.negativeParticleAreaMult, Misc.random);
                }

                float dur = baseDuration + baseDuration * (float) Math.random();
                dur += extraDur;
                float nSize = size;
                Vector2f pt = Misc.getPointWithinRadius(point, nSize * 0.5f);
                Vector2f v = Misc.getUnitVectorAtDegreeAngle((float) Math.random() * 360f);
                v.scale(nSize + nSize * (float) Math.random() * 0.5f);
                v.scale(0.2f * shroudParams.negativeParticleVelMult);
                Vector2f.add(vel, v, v);

                float maxSpeed = nSize * 1.5f * 0.2f;
                float minSpeed = nSize * 1f * 0.2f;
                float overMin = v.length() - minSpeed;
                if (overMin > 0) {
                    float durMult = 1f - overMin / (maxSpeed - minSpeed);
                    if (durMult < 0.1f) durMult = 0.1f;
                    dur *= 0.5f + 0.5f * durMult;
                }

                dur *= shroudParams.negativeParticleDurMult;

                //nSize *= 1.5f;

                engine.addNegativeNebulaParticle(pt, v, nSize * 1f * shroudParams.negativeParticleSizeMult, 2f,
                        0.5f / dur, 0f, dur, c);
            }
            Global.getCombatEngine().getViewport().setEverythingNearViewport(false);
        }

    }

    public VoidlingShroudParams getShroudParams() {
        return shroudParams;
    }
}