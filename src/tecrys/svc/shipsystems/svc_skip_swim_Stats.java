package tecrys.svc.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class svc_skip_swim_Stats extends BaseShipSystemScript {

	static final Float SPEED_MODIFIER = 1800f;
	static final Float ACCEL_MODIFIER = 2500f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			stats.getAcceleration().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyFlat(id, SPEED_MODIFIER);
			stats.getAcceleration().modifyFlat(id, ACCEL_MODIFIER * effectLevel);
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData(ACCEL_MODIFIER.toString() , false);
		} else if (index == 1) {
			return new StatusData(SPEED_MODIFIER.toString(), false);
		}
		return null;
	}
}
