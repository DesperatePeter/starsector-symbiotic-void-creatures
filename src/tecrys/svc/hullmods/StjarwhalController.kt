package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import tecrys.svc.WHALE_REPUTATION_MIN
import tecrys.svc.internalWhaleReputation

class StjarwhalController: BaseHullMod() {
    override fun advanceInCampaign(member: FleetMemberAPI?, amount: Float) {
        member?.let {
            it.stats.suppliesPerMonth.modifyMult(this.javaClass.name, computeMaintenanceFactor())
        }
    }

    private fun computeMaintenanceFactor(): Float{
        if(internalWhaleReputation > WHALE_REPUTATION_MIN) return 1f
        return 1f + (100f - internalWhaleReputation) / 100f
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return when(index){
            0 -> "${(computeMaintenanceFactor() * 100f).toInt()}%"
            1 -> "${internalWhaleReputation.toInt()}"
            else -> null
        }
    }
}