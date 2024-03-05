package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI

class VoidlingInfestation: BaseHullMod() {
    companion object{
        const val TRIGGER_HULL_LEVEL = 0.5f
        const val FIGHTER_ID = "svc_mios_mandibles_wing"
        val NUMBER_OF_FIGHTERS = mapOf(
            ShipAPI.HullSize.FIGHTER to 1,
            ShipAPI.HullSize.FRIGATE to 1,
            ShipAPI.HullSize.DESTROYER to 2,
            ShipAPI.HullSize.CRUISER to 3,
            ShipAPI.HullSize.CAPITAL_SHIP to 4
        )
    }

    private val alreadyTriggered = mutableSetOf<String>()
    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        val id = ship?.id ?: return
        if(id in alreadyTriggered) return
        if(ship.hullLevel <= TRIGGER_HULL_LEVEL){
            spawnFighters(ship)
            alreadyTriggered.add(ship.id)
        }
    }

    private fun spawnFighters(ship: ShipAPI){
        for(i in 0 until (NUMBER_OF_FIGHTERS[ship.hullSize] ?: 0)){
            Global.getCombatEngine().getFleetManager(ship.originalOwner).spawnShipOrWing(
                FIGHTER_ID, ship.location, ship.facing + 30f * i.toFloat()
            )
        }
    }
}