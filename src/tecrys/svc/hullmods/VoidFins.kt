package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import java.util.*

class VoidFins: BiologicalBaseHullmod() {
    companion object{
        const val MULT_SRC = "SVC_VOID_FINS"
        private const val VARIATION_PERCENT: Float = 0.15f // 15% variation
    }
    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if (stats!!.fleetMember == null) {
            return
        }
        val uid = stats!!.fleetMember.id

        val random = Random(uid.hashCode().toLong()) // Seeded random for consistent variation per ship


        stats!!.armorBonus.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.hullBonus.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.maxSpeed.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.acceleration.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.deceleration.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.maxTurnRate.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.turnAcceleration.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.fluxDissipation.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.fluxCapacity.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.cargoMod.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.crPerDeploymentPercent.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.peakCRDuration.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.baseCRRecoveryRatePercentPerDay.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.suppliesPerMonth.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.sensorProfile.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.sensorStrength.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats!!.suppliesToRecover.modifyMult(id, 1f + ((random.nextFloat() * 2 - 1) * VARIATION_PERCENT))
        stats?.fuelUseMod?.modifyMult(MULT_SRC, 0f)
    }
}