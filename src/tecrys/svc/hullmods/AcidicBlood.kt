package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.hullmods.listeners.AcidicBloodListener

class AcidicBlood: BiologicalBaseHullmod() {

    companion object{
        const val FIGHTER = 50f
        const val FRIGATE = 100f
        const val DESTROYER = 200f
        const val CRUISER = 350f
        const val CAPITAL_SHIP = 500f
    }
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        ship?.run {
            addListener(AcidicBloodListener(this))
        }
    }
    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return when(index){
            0 -> "${(AcidicBlood.FIGHTER).toInt()}"
            1 -> "${(AcidicBlood.FRIGATE).toInt()}"
            2 -> "${(AcidicBlood.DESTROYER).toInt()}"
            3 -> "${(AcidicBlood.CRUISER).toInt()}"
            4 -> "${(AcidicBlood.CAPITAL_SHIP).toInt()}"
            else -> null
        }
    }
}