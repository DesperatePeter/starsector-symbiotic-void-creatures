package tecrys.svc.shipsystems;
import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.ShockwaveVisual;
import com.fs.starfarer.api.impl.combat.threat.EnergyLashActivatedSystem;
import com.fs.starfarer.api.impl.combat.threat.ThreatSwarmAI;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI.SystemState;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.FindShipFilter;

import static tecrys.svc.utils.UtilsKt.randomizeColor;


public class svc_phase_flume_stats extends BaseShipSystemScript {

    public static float MAX_LASH_RANGE = 500f;



    public static float MIN_COOLDOWN = 2f;
    public static float MAX_COOLDOWN = 10f;
    public static float COOLDOWN_DP_MULT = 0.33f;

    public static float MIN_HIT_ENEMY_COOLDOWN = 2f;
    public static float MAX_HIT_ENEMY_COOLDOWN = 5f;
    public static float HIT_PHASE_ENEMY_COOLDOWN_MULT = 2f;

    public static float SWARM_TIMEOUT = 10f;

    public static float PHASE_OVERLOAD_DUR = 1f;

    public static Color SHROUD_COLOR = new Color(151, 170, 25, 180);
    public static Color SHROUD_GLOW_COLOR = new Color(92, 85, 24, 180);

    public EffectPlugin effectPlugin;

    public static class EffectPlugin extends BaseEveryFrameCombatPlugin {
        private float duration = 1f;


        private static float DAMAGE = 10000;
        private static float EMP_DAMAGE = 20000;
        private ShipAPI target;
        private ShipAPI user;
        private float lastRenderTime = 0f;
        private float startTime = 0f;
        private Boolean active = false;
        private float interval = 0.3f;

        private final Random rng = new Random();
        // one in CHANCE_TO_BYPASS_SHIELD chance to bypass shield
        private final int CHANCE_TO_BYPASS_SHIELD = 5;

        public void setTarget(ShipAPI target) {
            this.target = target;
        }

        public void setUser(ShipAPI user) {
            this.user = user;
            this.duration = user.getSystem().getChargeActiveDur();
        }

        public void setActive(Boolean active) {
            this.active = active;

            if (active) {
                Global.getSoundPlayer().playSound("energy_lash_fire_at_enemy", 1f, 1f, user.getLocation(), user.getVelocity());
                this.startTime = Global.getCombatEngine().getTotalElapsedTime(false);
            }
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            CombatEngineAPI engine = Global.getCombatEngine();

            if(engine.isPaused() || target == null){
                this.setActive(false);
                return;
            }

            float currentTime = engine.getTotalElapsedTime(false);
            float maxTime = startTime + duration - currentTime;

            if (maxTime <= 0f) {
                this.setActive(false);
                return;
            }

            // draw nebula particles
            if (lastRenderTime + interval < currentTime) {
                lastRenderTime = currentTime;
                Global.getCombatEngine().addNebulaParticle(
                        target.getLocation(),
                        target.getVelocity(),
                        target.getCollisionRadius() * 1.5f, // size
                        1f, // endSizeMult
                        0.6f, // rampUpFraction
                        2f, // fullBrightnessFraction
                        interval * 4 + 0.2f, // totalDuration
                        randomizeColor(SHROUD_COLOR, 30),
                        false
                );
            }

            if (target == null || target.getOwner() == user.getOwner()) {
                this.setActive(false);
                return;
            }

            if (!user.getSystem().isStateActive()){
                return;
            }

            if (!target.isAlive()){
                target = findTarget(user);

                if (target == null) {
                    this.setActive(false);
                    return;
                }
            }

            // deal damage
            int rand = rng.nextInt(1, CHANCE_TO_BYPASS_SHIELD);

            DamageType damageType = DamageType.ENERGY;

            if (user.getSystem().getSpecAPI().getDamageType() != null) {
                damageType = user.getSystem().getSpecAPI().getDamageType();
            }

            engine.applyDamage(
                    target,
                    target.getLocation(),
                    user.getSystem().getSpecAPI().getDamage() * amount,
                    damageType,
                    user.getSystem().getSpecAPI().getEmpDamage() * amount,
                    rand == CHANCE_TO_BYPASS_SHIELD,
                    true,
                    user,
                    false
            );
        }

    }



//    protected WeaponSlotAPI mainSlot;
//    protected List<WeaponSlotAPI> slots;
    protected float sinceSwarmTargeted = SWARM_TIMEOUT;
    protected float cooldownToSet = -1f;

    public svc_phase_flume_stats() {
        effectPlugin = new EffectPlugin();
        Global.getCombatEngine().addPlugin(effectPlugin);
    }

//    protected void findSlots(ShipAPI ship) {
//        if (slots != null) return;
//        slots = new ArrayList<>();
//        for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
//            if (slot.isSystemSlot()) {
//                slots.add(slot);
//                if (slot.getSlotSize() == WeaponSize.MEDIUM) {
//                    mainSlot = slot;
//                }
//            }
//        }
//    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = null;
        //boolean player = false;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            //player = ship == Global.getCombatEngine().getPlayerShip();
        } else {
            return;
        }

        if (effectPlugin.user == null) {
            effectPlugin.setUser(ship);
        }

        sinceSwarmTargeted += Global.getCombatEngine().getElapsedInLastFrame();

        if ((state == State.COOLDOWN || state == State.IDLE) && cooldownToSet >= 0f) {
            ship.getSystem().setCooldown(cooldownToSet);
            ship.getSystem().setCooldownRemaining(cooldownToSet);
            cooldownToSet = -1f;

        }

        if (state == State.IN || state == State.OUT) {
            float jitterLevel = effectLevel;

            float jitterRangeBonus = getJitterRangeBonus(state, effectLevel);
            Color color = SHROUD_COLOR;
            ship.setJitter(this, color, jitterLevel, 5, 0f, 3f + jitterRangeBonus);
        }

        if (effectLevel == 1 && !effectPlugin.active) {
            ShipAPI target = findTarget(ship);
            if (target != null) {
                System.out.println("setting active");
                effectPlugin.setTarget(target);
                effectPlugin.setActive(true);
                applyEffectToTarget(ship, target);
            }
        }
    }

    private static float getJitterRangeBonus(State state, float effectLevel) {
        float maxRangeBonus = 150f;
        //float jitterRangeBonus = jitterLevel * maxRangeBonus;
        float jitterRangeBonus = (1f - effectLevel * effectLevel) * maxRangeBonus;

        float brightness = 0f;
        float threshold = 0.1f;
        if (effectLevel < threshold) {
            brightness = effectLevel / threshold;
        } else {
            brightness = 1f - (effectLevel - threshold) / (1f - threshold);
        }
        if (brightness < 0) brightness = 0;
        if (brightness > 1) brightness = 1;
        if (state == State.OUT) {
            jitterRangeBonus = 0f;
            brightness = effectLevel * effectLevel;
        }
        return jitterRangeBonus;
    }


    protected void applyEffectToTarget(ShipAPI ship, ShipAPI target) {
        boolean isSwarm = ThreatSwarmAI.isAttackSwarm(target);
        if (!isSwarm) {
            if (target == null || target.getSystem() == null || target.isHulk()) return;
        }
        if (ship == null || ship.getSystem() == null || ship.isHulk()) return;

        if (ship.getOwner() == target.getOwner()) {
            if (target.getSystem() != null && target.getSystem().getScript() instanceof EnergyLashActivatedSystem) {
//                EnergyLashActivatedSystem script = (EnergyLashActivatedSystem) target.getSystem().getScript();
//                script.hitWithEnergyLash(ship, target);
            } else if (isSwarm) {
//                VoltaicDischargeOnFireEffect.setSwarmPhaseMode(target);
//                sinceSwarmTargeted = 0f;
            }

            float cooldown = target.getHullSpec().getSuppliesToRecover();
            //float cooldown = target.getMutableStats().getSuppliesToRecover().getBaseValue();
            //cooldown = (int)Math.round(target.getMutableStats().getDynamic().getMod(Stats.DEPLOYMENT_POINTS_MOD).computeEffective(cooldown));

            cooldown = MIN_COOLDOWN + cooldown * COOLDOWN_DP_MULT;
            if (cooldown > MAX_COOLDOWN) cooldown = MAX_COOLDOWN;
            if (target.isFighter()) cooldown = MIN_COOLDOWN;
//			ship.getSystem().setCooldown(cooldown);
//			ship.getSystem().setCooldownRemaining(cooldown);
            cooldownToSet = cooldown;
        } else {
            boolean hitPhase = false;
            if (target.isPhased()) {
                target.setOverloadColor(SHROUD_COLOR);
                target.getFluxTracker().beginOverloadWithTotalBaseDuration(PHASE_OVERLOAD_DUR);
                if (target.getFluxTracker().showFloaty() ||
                        ship == Global.getCombatEngine().getPlayerShip() ||
                        target == Global.getCombatEngine().getPlayerShip()) {
                    target.getFluxTracker().playOverloadSound();
                    target.getFluxTracker().showOverloadFloatyIfNeeded("Phase Field Disruption!",
                            SHROUD_COLOR, 4f, true);
                }
                ShockwaveVisual.ShockwaveParams params = new ShockwaveVisual.ShockwaveParams();
                params.color = new Color(92, 85, 24, 255);
                params.radius = (target.getCollisionRadius() + 2f);
                params.loc = target.getLocation();


                ShockwaveVisual.spawnShockwave(params);
                Global.getCombatEngine().addPlugin(new BaseEveryFrameCombatPlugin() {
                    @Override
                    public void advance(float amount, List<InputEventAPI> events) {
                        if (!target.getFluxTracker().isOverloadedOrVenting()) {
                            target.resetOverloadColor();
                            Global.getCombatEngine().removePlugin(this);
                        }
                    }
                });

                hitPhase = true;
            }

            float cooldown = MIN_HIT_ENEMY_COOLDOWN +
                    (MAX_HIT_ENEMY_COOLDOWN - MIN_HIT_ENEMY_COOLDOWN) * (float) Math.random();
            if (hitPhase) {
                cooldown *= HIT_PHASE_ENEMY_COOLDOWN_MULT;
            }
            if (cooldown > MAX_COOLDOWN) cooldown = MAX_COOLDOWN;
//			ship.getSystem().setCooldown(cooldown);
//			ship.getSystem().setCooldownRemaining(cooldown);
            cooldownToSet = cooldown;
        }

//		ship.getSystem().setCooldown(0.2f);
//		ship.getSystem().setCooldownRemaining(0.2f);
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != SystemState.IDLE) return null;
        if (ship == null) return null;


        ShipAPI target = findTarget(ship);

        if(target == null || ship.getShipTarget() == null) {
            return "NO TARGET";
        }
        if (ship.getShipTarget().getOwner() == ship.getOwner()) {
            return "FRIENDLY TARGET";
        }
        if (target != ship) {
            return "READY";
        }
        if (ship.getShipTarget() != null) {
            return "OUT OF RANGE";
        }
        return "NO TARGET";
    }

    public boolean isInRange(ShipAPI ship, ShipAPI target) {
        float range = getRange(ship);
        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
        return dist <= range + radSum;
    }

    public static boolean isValidLashTarget(ShipAPI ship, ShipAPI other) {
        if (other == null) return false;
        if (other.isHulk() || other.getOwner() == 100) return false;
        if (other.isShuttlePod()) return false;
        if (other.isFighter() && other.getOwner() == ship.getOwner()) {

        }

        if (other.isFighter()) return false;
//        if (other.getOwner() == ship.getOwner()) {
//            if (other.getSystem() == null) return false;
//            if (!(other.getSystem().getScript() instanceof EnergyLashActivatedSystem)) return false;
//            if (other.getSystem().getCooldownRemaining() > 0) return false;
//            if (other.getSystem().isActive()) return false;
//            if (other.getFluxTracker().isOverloadedOrVenting()) return false;
//        }
        return true;
        //return !other.isFighter();
    }


    public static ShipAPI findTarget(ShipAPI ship) {
        float range = getRange(ship);
        boolean player = ship == Global.getCombatEngine().getPlayerShip();
        ShipAPI target = ship.getShipTarget();

        float extraRange = 0f;
        if (ship.getShipAI() != null && ship.getAIFlags().hasFlag(AIFlags.CUSTOM1)){
            target = (ShipAPI) ship.getAIFlags().getCustom(AIFlags.CUSTOM1);
            extraRange += 500f;
        }


        if (target != null) {
            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
            if (dist > range + radSum + extraRange) target = null;
        } else {
            FindShipFilter filter = s -> isValidLashTarget(ship, s);

            if (target == null || target.getOwner() != ship.getOwner()) {
                if (player) {
                    target = Misc.findClosestShipTo(ship, ship.getMouseTarget(), HullSize.FIGHTER, range, true, false, filter);
                } else {
                    Object test = ship.getAIFlags().getCustom(AIFlags.MANEUVER_TARGET);
                    if (test instanceof ShipAPI) {
                        target = (ShipAPI) test;
                        float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
                        float radSum = ship.getCollisionRadius() + target.getCollisionRadius();
                        if (dist > range + radSum) target = null;
                    }
                }
            }
            if (target == null) {
                target = Misc.findClosestShipTo(ship, ship.getLocation(), HullSize.FIGHTER, range, true, false, filter);
            }
        }

        return target;
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        ShipAPI target = findTarget(ship);
        return target != null && target != ship && (target.getOwner() != ship.getOwner());
        //return super.isUsable(system, ship);
    }

    public static float getRange(ShipAPI ship) {
        if (ship == null) return MAX_LASH_RANGE;
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(MAX_LASH_RANGE);
    }

}












