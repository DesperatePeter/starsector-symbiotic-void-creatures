package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.hullmods.listeners.AcidicBloodListener

class AcidicBlood: BiologicalBaseHullmod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        ship?.run {
            addListener(AcidicBloodListener(this))
        }
    }
}