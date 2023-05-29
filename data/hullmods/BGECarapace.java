package data.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BGECarapace extends BaseHullMod {
  

        private static final Set BLOCKED_HULLMODS = new HashSet();
        public static final float DRV_HEALTH_BONUS = 1f;
	public static final float WEP_HEALTH_BONUS = 100f;
	public static final float FLUX_RESISTANCE = 20f;
        public static final float HiEx_RESISTANCE = 0.9f;

	  static
    {
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
                stats.getWeaponHealthBonus().modifyPercent(id, WEP_HEALTH_BONUS);
		        stats.getEngineHealthBonus().modifyPercent(id, WEP_HEALTH_BONUS);
	         	stats.getEmpDamageTakenMult().modifyMult(id, 0.5f - FLUX_RESISTANCE * 0.01f);
                stats.getMaxCombatHullRepairFraction().modifyFlat(id, 1f);
		/*stats.getArmorDamageTakenMult().modifyPercent(id, - 50f);*/
                stats.getMaxArmorDamageReduction().modifyFlat(id, 0.6f);
				stats.getMinArmorFraction().modifyFlat(id, 0.4f);
                
	}
	    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        String tmp;
        for (Iterator iter = BLOCKED_HULLMODS.iterator(); iter.hasNext();)
        {
            tmp = (String) iter.next();
            if (ship.getVariant().getHullMods().contains(tmp))
            {
                ship.getVariant().removeMod(tmp);
            }
        }
    }
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) DRV_HEALTH_BONUS;
		if (index == 1) return "" + (int) WEP_HEALTH_BONUS;
		if (index == 2) return "" + (int) FLUX_RESISTANCE;
		return null;
	}




}
