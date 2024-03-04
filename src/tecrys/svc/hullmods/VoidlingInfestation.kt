package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI

class VoidlingInfestation: BiologicalBaseHullmod() {
    companion object{
        const val TRIGGER_HULL_LEVEL = 0.5f
        const val FIGHTER_ID = "svc_mios_mandibles_wing"
        const val NUMBER_OF_FIGHTERS = 3
    }

    private val alreadyTriggered = mutableListOf<String>()
    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        val id = ship?.id ?: return
        if(id in alreadyTriggered) return
        if(ship.hullLevel <= TRIGGER_HULL_LEVEL) spawnFighters(ship)
    }

    private fun spawnFighters(ship: ShipAPI){
        for(i in 0 until NUMBER_OF_FIGHTERS){
            Global.getCombatEngine().getFleetManager(ship.originalOwner).spawnShipOrWing(FIGHTER_ID, ship.location, ship.facing + 30f * i.toFloat())
        }
    }
}