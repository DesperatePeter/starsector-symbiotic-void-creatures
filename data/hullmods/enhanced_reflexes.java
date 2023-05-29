package data.hullmods;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class enhanced_reflexes extends BaseHullMod {

        public static final float ROF_BONUS_PERCENT = 1.5f;
	public static final float PEAK_BONUS_PERCENT = 90f;
	public static final float DEGRADE_REDUCTION_PERCENT = 0.1f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getMissileRoFMult().modifyMult(id, ROF_BONUS_PERCENT);
		stats.getBallisticRoFMult().modifyMult(id, ROF_BONUS_PERCENT);
		stats.getEnergyRoFMult().modifyMult(id, ROF_BONUS_PERCENT);
		stats.getPeakCRDuration().modifyFlat(id, PEAK_BONUS_PERCENT);
		stats.getCRLossPerSecondPercent().modifyFlat(id, 1f - DEGRADE_REDUCTION_PERCENT * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) PEAK_BONUS_PERCENT + "%";
		if (index == 1) return "" + (int) DEGRADE_REDUCTION_PERCENT + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && (ship.getHullSpec().getNoCRLossTime() < 200 || ship.getHullSpec().getCRLossPerSecond() == 0) && ship.getVariant().getHullMods().contains("BGECarapace"); 
	}
}
