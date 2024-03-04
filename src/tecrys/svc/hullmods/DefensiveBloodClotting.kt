package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.hullmods.listeners.DefensiveBloodListener

class DefensiveBloodClotting: BiologicalBaseHullmod() {
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        ship?.addListener(DefensiveBloodListener(ship))
    }
}