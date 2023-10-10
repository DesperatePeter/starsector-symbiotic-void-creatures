package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.shipsystems.Parry
import tecrys.svc.utils.isUsable

class ParryAI: ShipSystemAIScript {
    companion object{
        // should include
        const val TIME_TO_IMPACT_BUFFER = 0.5f // activate system if time to impact of relevant projectiles less than this
        const val PROJECTILE_SCAN_RANGE = 400f // higher values -> worse performance
        const val COLLISION_RADIUS_TOLERANCE = 1.25f
        const val DAMAGE_TO_HULL_THRESHOLD = 0.2f // parry if expected to take this*hullPoints within TIME_TO_IMPACT_BUFFER
    }


    private var shipOpt: ShipAPI? = null
    private var systemOpt: ShipSystemAPI? = null
    private var engineOpt: CombatEngineAPI? = null
    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.shipOpt = ship
        this.systemOpt = system
        this.engineOpt = engine
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val ship = shipOpt ?: return
        val sys = (if(systemOpt?.id == "parry") systemOpt else ship.phaseCloak) ?: return// systemOpt ?: return
        if(!sys.isUsable()) return

        val projectiles = CombatUtils.getProjectilesWithinRange(ship.location, ship.collisionRadius + PROJECTILE_SCAN_RANGE)
        val missiles = CombatUtils.getMissilesWithinRange(ship.location, ship.collisionRadius + PROJECTILE_SCAN_RANGE).filter {
            !it.isGuided
        }

        var threat = 0f

        (projectiles + missiles).filter {
            it.owner != ship.originalOwner
        }.filter {
            CollisionUtils.getCollides(it.location, it.location + it.velocity, ship.location,
                ship.collisionRadius + COLLISION_RADIUS_TOLERANCE)
        } .filter {
            (ship.location - it.location).length() <= it.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius
        }.forEach {
            threat += it.damageAmount
        }

        if(sys.ammo.toFloat() * threat > DAMAGE_TO_HULL_THRESHOLD * ship.hitpoints){
            sys.forceState(ShipSystemAPI.SystemState.IN, 0f)
            sys.ammo -= 1
        }

    }
}