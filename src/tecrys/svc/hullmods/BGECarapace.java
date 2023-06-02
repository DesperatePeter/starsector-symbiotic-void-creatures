package tecrys.svc.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import org.lwjgl.util.vector.Vector2f;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

public class BGECarapace extends BaseHullMod {

    public static final float RESISTANCE = 0.01f;
    public static final float RESIST_TIME = 0.2f; //in seconds

    private Set<ShipAPI> nearbyShips = new HashSet<>();
    private float resisting = 0f;

    private static final Set BLOCKED_HULLMODS = new HashSet();
    public static final float DRV_HEALTH_BONUS = 1f;
    public static final float WEP_HEALTH_BONUS = 100f;
    public static final float FLUX_RESISTANCE = 20f;
    public static final float HiEx_RESISTANCE = 0.9f;

    static {
        // These hullmods will automatically be removed
        // Not as elegant as blocking them in the first place, but
        // this method doesn't require editing every hullmod's script
        BLOCKED_HULLMODS.add("turretgyros");
        BLOCKED_HULLMODS.add("advancedoptics");
        BLOCKED_HULLMODS.add("autorepair");
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("targetingunit");
        BLOCKED_HULLMODS.add("augmentedengines");
        BLOCKED_HULLMODS.add("blast_doors");
        BLOCKED_HULLMODS.add("unstable_injector");
        BLOCKED_HULLMODS.add("reinforcedhull");
        BLOCKED_HULLMODS.add("heavyarmor");
        BLOCKED_HULLMODS.add("fluxshunt");
        BLOCKED_HULLMODS.add("auxiliarythrusters");
        BLOCKED_HULLMODS.add("insulatedengine");
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        /*        stats.getWeaponHealthBonus().modifyPercent(id, WEP_HEALTH_BONUS);*/
 /*stats.getEngineHealthBonus().modifyPercent(id, WEP_HEALTH_BONUS);*/
        stats.getEmpDamageTakenMult().modifyMult(id, 0.5f);
        stats.getBeamDamageTakenMult().modifyMult(id, 0.7f);
        stats.getProjectileDamageTakenMult().modifyMult(id, 0.7f);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 0.3f);
        stats.getHullCombatRepairRatePercentPerSecond().modifyFlat(id, 2f);
        stats.getMaxCombatHullRepairFraction().modifyFlat(id, 1f);
        stats.getZeroFluxSpeedBoost().modifyMult(id, 0f);
		stats.getDynamic().getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 0f);
		stats.getDynamic().getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 0f);        

    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        if (Global.getCombatEngine() == null) {
            return;
        }

        List<ShipAPI> shipPlusModules = ship.getChildModulesCopy();
        shipPlusModules.add(ship);

        resisting -= amount;
        for (ShipAPI s : Global.getCombatEngine().getShips()) {
            if (s != ship && s.getHullSize() != HullSize.FIGHTER && s.isAlive()) {
                float distance = Vector2f.sub(ship.getLocation(), s.getLocation(), new Vector2f()).length() - ship.getCollisionRadius();
                for (ShipAPI child : ship.getChildModulesCopy()) {
                    float newDistance = Vector2f.sub(child.getLocation(), s.getLocation(), new Vector2f()).length() - child.getCollisionRadius();
                    distance = Math.min(distance, newDistance);
                }
                float mult = s.getMutableStats().getDynamic().getValue(Stats.EXPLOSION_RADIUS_MULT);
                float radius = s.getCollisionRadius() + Math.min(200f, s.getCollisionRadius()) * mult;
                if (distance <= radius) {
                    nearbyShips.add(s);
                } else {
                    nearbyShips.remove(s);
                }
            }
        }
        Iterator<ShipAPI> iter = nearbyShips.iterator();
        while (iter.hasNext()) {
            ShipAPI t = iter.next();
            if (t == null || !t.isAlive()) {
                iter.remove();
                for (ShipAPI s : shipPlusModules) {
                    s.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult("BGECarapace", RESISTANCE);
                    s.getMutableStats().getHighExplosiveShieldDamageTakenMult().modifyMult("BGECarapace", RESISTANCE);
                }
                resisting = RESIST_TIME;
            }
        }
        if (resisting <= 0f) {
            for (ShipAPI s : shipPlusModules) {
                s.getMutableStats().getHighExplosiveDamageTakenMult().unmodify("BGECarapace");
                s.getMutableStats().getHighExplosiveShieldDamageTakenMult().unmodify("BGECarapace");
            }
        }

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        String tmp;
        for (Iterator iter = BLOCKED_HULLMODS.iterator(); iter.hasNext();) {
            tmp = (String) iter.next();
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) DRV_HEALTH_BONUS;
        }
        if (index == 1) {
            return "" + (int) WEP_HEALTH_BONUS;
        }
        if (index == 2) {
            return "" + (int) FLUX_RESISTANCE;
        }
        return null;
    }

}
