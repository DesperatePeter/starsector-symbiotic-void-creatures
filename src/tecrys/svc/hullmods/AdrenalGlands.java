package tecrys.svc.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.Misc;

public class AdrenalGlands extends BiologicalBaseHullmod {

	private static Map speed = new HashMap();
	static {
		speed.put(HullSize.FRIGATE, 50f);
		speed.put(HullSize.DESTROYER, 30f);
		speed.put(HullSize.CRUISER, 20f);
		speed.put(HullSize.CAPITAL_SHIP, 10f);
	}

	private static final float PEAK_MULT = 0.5f;

	private static final float FLUX_DISSIPATION_MULT = 2f;

	private static final float MELEE_MULT = 1.5f;
	

	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, (Float) speed.get(hullSize));
		stats.getBallisticWeaponDamageMult().modifyMult(id, MELEE_MULT);
		stats.getArmorDamageTakenMult().modifyMult(id, MELEE_MULT);
		stats.getHullCombatRepairRatePercentPerSecond().modifyMult(id, FLUX_DISSIPATION_MULT);
		stats.getFluxDissipation().modifyMult(id, FLUX_DISSIPATION_MULT);
        stats.getMaxTurnRate().modifyMult(id, MELEE_MULT);
        stats.getTurnAcceleration().modifyMult(id, MELEE_MULT);
		stats.getPeakCRDuration().modifyMult(id, PEAK_MULT);

		stats.getVentRateMult().modifyMult(id, 0f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) speed.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) speed.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) speed.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) speed.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return Misc.getRoundedValue(FLUX_DISSIPATION_MULT);
		if (index == 5) return "" + ((Float) MELEE_MULT);
		//if (index == 4) return Strings.X + Misc.getRoundedValue(PEAK_MULT);
		if (index == 6) return Misc.getRoundedValue(FLUX_DISSIPATION_MULT);
		if (index == 7) return "" + ((Float) MELEE_MULT);
		if (index == 8) return "Grappler Tendrils";
		if (index == 9) return "Tentacle Whips";
		//if (index == 3) return Misc.getRoundedValue(CR_DEG_MULT);
//		if (index == 5) return Misc.getRoundedValue(RANGE_THRESHOLD);
//		if (index == 4) return Misc.getRoundedValue(RECOIL_MULT);
		//if (index == 3) return (int)OVERLOAD_DUR + "%";
		
//		if (index == 0) return "" + ((Float) speed.get(hullSize)).intValue();
//		if (index == 1) return "" + (int)((FLUX_DISSIPATION_MULT - 1f) * 100f) + "%";
//		if (index == 2) return "" + (int)((1f - PEAK_MULT) * 100f) + "%";
		
//		if (index == 0) return "" + ((Float) speed.get(HullSize.FRIGATE)).intValue();
//		if (index == 1) return "" + ((Float) speed.get(HullSize.DESTROYER)).intValue();
//		if (index == 2) return "" + ((Float) speed.get(HullSize.CRUISER)).intValue();
//		if (index == 3) return "" + ((Float) speed.get(HullSize.CAPITAL_SHIP)).intValue();
//		
//		if (index == 4) return "" + (int)((FLUX_DISSIPATION_MULT - 1f) * 100f);
//		if (index == 5) return "" + (int)((1f - PEAK_MULT) * 100f);
		
		return null;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
//		return !ship.getVariant().getHullMods().contains("unstable_injector") &&
//			   !ship.getVariant().getHullMods().contains("augmented_engines");

		List<WeaponAPI> wep = ship.getAllWeapons();
		for (WeaponAPI weprange : wep) {
			if (
			!weprange.getOriginalSpec().hasTag("melee") && !weprange.getType().equals(WeaponAPI.WeaponType.DECORATIVE)
			)
				return false;
		}
		
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {

		List<WeaponAPI> wep = ship.getAllWeapons();
		for (WeaponAPI weprange : wep) {
			if (
					!weprange.getOriginalSpec().hasTag("melee") && !weprange.getType().equals(WeaponAPI.WeaponType.DECORATIVE)
			)
				return "One or more ranged weapons found.";
		}
		
		return null;
	}
	

	private Color color = new Color(255,100,255,255);
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		//ship.getFluxTracker().setHardFlux(ship.getFluxTracker().getCurrFlux());
//		if (ship.getEngineController().isAccelerating() || 
//				ship.getEngineController().isAcceleratingBackwards() ||
//				ship.getEngineController().isDecelerating() ||
//				ship.getEngineController().isTurningLeft() ||
//				ship.getEngineController().isTurningRight() ||
//				ship.getEngineController().isStrafingLeft() ||
//				ship.getEngineController().isStrafingRight()) {
			ship.getEngineController().fadeToOtherColor(this, color, null, 1f, 0.4f);
			ship.getEngineController().extendFlame(this, 0.25f, 0.25f, 0.25f);
//		}
	}

	

}
