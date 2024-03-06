package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.ext.plus
import org.magiclib.kotlin.createDefaultShipAI
import tecrys.svc.hullmods.listeners.KillSwitch
import tecrys.svc.utils.vectorFromAngleDeg
import java.awt.Color

class VoidlingInfestation: BaseHullMod() {
    companion object{
        const val TRIGGER_HULL_LEVEL = 0.5f
        const val FIGHTER_ID = "svc_mios_mandibles_single_wing"
        val NUMBER_OF_FIGHTERS = mapOf(
            ShipAPI.HullSize.FIGHTER to 1,
            ShipAPI.HullSize.FRIGATE to 2,
            ShipAPI.HullSize.DESTROYER to 4,
            ShipAPI.HullSize.CRUISER to 7,
            ShipAPI.HullSize.CAPITAL_SHIP to 11
        )
        val CLOUD_COLOR: Color = Color.RED
        const val CLOUD_RADIUS = 200f
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
        val fleetMan = Global.getCombatEngine().getFleetManager(ship.originalOwner)
        val wasSpawnMsgSuppressed = fleetMan.isSuppressDeploymentMessages
        fleetMan.isSuppressDeploymentMessages = true
        for(i in 0 until (NUMBER_OF_FIGHTERS[ship.hullSize] ?: 0)){
            val facing = Math.random().toFloat() * 360f
            val offset = vectorFromAngleDeg(facing)
            offset.scale(ship.collisionRadius * 0.75f)
            val loc = ship.location + offset
            val fighter = fleetMan.spawnShipOrWing(
                FIGHTER_ID, loc, facing
            )
            fighter.shipAI = fighter.createDefaultShipAI(ShipAIConfig())
            Global.getCombatEngine().addPlugin(KillSwitch(fighter, 30f))
            Global.getCombatEngine().addNebulaParticle(
                loc, ship.velocity, CLOUD_RADIUS, 2f, 0.5f, 0.8f, CLOUD_DURATION, CLOUD_COLOR
            )
        }
        fleetMan.isSuppressDeploymentMessages = wasSpawnMsgSuppressed
    }
}