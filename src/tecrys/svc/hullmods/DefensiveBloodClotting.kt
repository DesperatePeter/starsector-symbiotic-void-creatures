package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.hullmods.listeners.DefensiveBloodListener

class DefensiveBloodClotting: BiologicalBaseHullmod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        ship?.addListener(DefensiveBloodListener(ship))
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize, ship: ShipAPI?): String {
        return when(index){
            0 -> "${(DefensiveBloodListener.MAX_REDUCTION * 100f).toInt()}%"
            1 -> "${DefensiveBloodListener.MIN_VALUE[hullSize]}"
            2 -> "${DefensiveBloodListener.MAX_VALUE[hullSize]}"
            3 -> "${(DefensiveBloodListener.MAX_VALUE[hullSize] ?: 0f) / (DefensiveBloodListener.DECAY_PER_SEC[hullSize] ?: 1f)} "
            else -> ""
        }
    }
}