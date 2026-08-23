package tecrys.svc.shipsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class svc_skip_swim_Stats extends BaseShipSystemScript {

	private boolean didForceAI = false;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = (ShipAPI) stats.getEntity();

		if (state == State.OUT) {
			stats.getMaxSpeed().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
			didForceAI = false;
		} else {
			stats.getMaxSpeed().modifyFlat(id, 2500f);
			stats.getAcceleration().modifyFlat(id, 2500f * effectLevel);

			// Only force the AI evaluation once per activation
			if (!didForceAI && ship.getShipAI() != null) {
				ship.getShipAI().forceCircumstanceEvaluation();
				didForceAI = true;
			}
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		didForceAI = false;
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("improved maneuverability", false);
		} else if (index == 1) {
			return new StatusData("+50 top speed", false);
		}
		return null;
	}
}