package data.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class acceleratedmetabolism extends BaseHullMod {

	public static final float HULL_BONUS = 30f;
	

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, - HULL_BONUS);
		stats.getHullCombatRepairRatePercentPerSecond().modifyFlat(id, 2f);
		stats.getMaxCombatHullRepairFraction().modifyFlat(id, 100f);		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_BONUS;
		return null;
	}
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("BGECarapace");
	}
}
