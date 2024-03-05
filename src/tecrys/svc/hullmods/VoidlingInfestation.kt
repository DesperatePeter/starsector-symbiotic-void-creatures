package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ext.plus
import tecrys.svc.utils.vectorFromAngleDeg
import java.awt.Color

class VoidlingInfestation: BaseHullMod() {
    companion object{
        const val TRIGGER_HULL_LEVEL = 0.5f
        const val FIGHTER_ID = "svc_mios_mandibles"
        val NUMBER_OF_FIGHTERS = mapOf(
            ShipAPI.HullSize.FIGHTER to 1,
            ShipAPI.HullSize.FRIGATE to 2,
            ShipAPI.HullSize.DESTROYER to 4,
            ShipAPI.HullSize.CRUISER to 7,
            ShipAPI.HullSize.CAPITAL_SHIP to 9
        )
        val CLOUD_COLOR = Color.RED
        val CLOUD_RADIUS = 200f
        const val CLOUD_DURATION = 2f
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
            val facing = Math.random().toFloat() * 360f
            val offset = vectorFromAngleDeg(facing)
            val loc = ship.location + offset
            offset.scale(ship.collisionRadius * 0.75f)
            Global.getCombatEngine().getFleetManager(ship.originalOwner).spawnShipOrWing(
                FIGHTER_ID, loc, facing
            )
            Global.getCombatEngine().addNebulaParticle(
                loc, ship.velocity, CLOUD_RADIUS, 2f, 0.5f, 0.8f, CLOUD_DURATION, CLOUD_COLOR
            )
        }
    }
}