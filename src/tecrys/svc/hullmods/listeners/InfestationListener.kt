package tecrys.svc.hullmods.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.hullmods.VoidlingInfestation.Companion.CLOUD_COLOR
import tecrys.svc.hullmods.VoidlingInfestation.Companion.CLOUD_DURATION
import tecrys.svc.hullmods.VoidlingInfestation.Companion.CLOUD_RADIUS
import tecrys.svc.hullmods.VoidlingInfestation.Companion.FIGHTER_ID
import tecrys.svc.hullmods.VoidlingInfestation.Companion.NUMBER_OF_FIGHTERS
import tecrys.svc.hullmods.VoidlingInfestation.Companion.TRIGGER_HULL_LEVEL
import tecrys.svc.utils.vectorFromAngleDeg

// Note: A DamageTaken listener might be better, but this is tried and tested
class InfestationListener(private val ship: ShipAPI): DamageTakenModifier {
    companion object{
        const val ALREADY_TRIGGERED_MEM_KEY = "svc_infestation_has_triggered"
    }
    override fun modifyDamageTaken(
        param: Any?,
        target: CombatEntityAPI?,
        damage: DamageAPI?,
        point: Vector2f?,
        shieldHit: Boolean
    ): String? {
        if(ship.hullLevel < TRIGGER_HULL_LEVEL){
            spawnFighters()
            ship.removeListener(this)
            ship.setCustomData(ALREADY_TRIGGERED_MEM_KEY, true)
        }
        return null
    }

    private fun spawnFighters(){
        val fleetMan = Global.getCombatEngine().getFleetManager(ship.originalOwner)
        val wasSpawnMsgSuppressed = fleetMan.isSuppressDeploymentMessages
        fleetMan.isSuppressDeploymentMessages = true
        (0 until (NUMBER_OF_FIGHTERS[ship.hullSize] ?: 0)).forEach { i ->
            val facing = Math.random().toFloat() * 360f
            val offset = vectorFromAngleDeg(facing)
            offset.scale(ship.collisionRadius * 0.75f)
            val loc = ship.location + offset
            val fighter = fleetMan.spawnShipOrWing(
                FIGHTER_ID, loc, facing
            )
            fighter.resetDefaultAI()
            Global.getCombatEngine().addPlugin(KillSwitch(fighter, 30f))
            Global.getCombatEngine().addNebulaParticle(
                loc, ship.velocity, CLOUD_RADIUS, 2f, 0.5f, 0.8f, CLOUD_DURATION, CLOUD_COLOR
            )
        }
        fleetMan.isSuppressDeploymentMessages = wasSpawnMsgSuppressed
    }
}