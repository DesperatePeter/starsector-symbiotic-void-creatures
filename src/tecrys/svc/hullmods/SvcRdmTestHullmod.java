package tecrys.svc.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.util.Random;

public class SvcRdmTestHullmod extends BaseHullMod {

    private final float VARIATION_PERCENT = 0.1f; // 10% variation

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (stats.getFleetMember() == null) {return;}
        String uid = stats.getFleetMember().getId();

        Random random = new Random(uid.hashCode()); // Seeded random for consistent variation per ship

        float variation = ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT); // -10% to +10%

        stats.getArmorBonus().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getHullBonus().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getMaxSpeed().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getAcceleration().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getDeceleration().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getMaxTurnRate().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getTurnAcceleration().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getFluxDissipation().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));
        stats.getFluxCapacity().modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT));


    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        // No additional effects needed after ship creation
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) {return "Every Voidling is unique and thus their statistics differ positively or negatively by up to 10%.";}

        return null;
    }
}