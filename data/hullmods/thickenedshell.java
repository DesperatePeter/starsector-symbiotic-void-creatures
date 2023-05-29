package data.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class thickenedshell extends BaseHullMod {

	public static final float MANEUVER_BONUS = 30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getAcceleration().modifyPercent(id, - MANEUVER_BONUS * 2f);
		stats.getDeceleration().modifyPercent(id, - MANEUVER_BONUS);
		stats.getTurnAcceleration().modifyPercent(id, - MANEUVER_BONUS * 2f);
		stats.getMaxTurnRate().modifyPercent(id, - MANEUVER_BONUS);
		stats.getArmorDamageTakenMult().modifyPercent(id, - MANEUVER_BONUS);	
		stats.getHullDamageTakenMult().modifyPercent(id, - MANEUVER_BONUS);	
}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) MANEUVER_BONUS;
		return null;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().getHullMods().contains("BGECarapace");
	}
}
