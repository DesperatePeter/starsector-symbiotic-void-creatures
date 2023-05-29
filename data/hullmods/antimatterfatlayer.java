package data.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class antimatterfatlayer extends BaseHullMod {

	public static final float HULL_BONUS = 40f;
	public static final float EXTRA_DAMAGE_TAKEN_PERCENT = 40f;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getWeaponDamageTakenMult().modifyPercent(id, EXTRA_DAMAGE_TAKEN_PERCENT);
		stats.getEngineDamageTakenMult().modifyPercent(id, EXTRA_DAMAGE_TAKEN_PERCENT);
		
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
