package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.combat.getNearbyEnemies
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.isUsable
import tecrys.svc.utils.toFloat

class DashAI: ShipSystemAIScript {
    companion object{
        // should include
        const val TIME_TO_IMPACT_BUFFER = 0.5f // activate system if time to impact of relevant projectiles less than this
        const val PROJECTILE_SCAN_RANGE = 400f // higher values -> worse performance
        const val COLLISION_RADIUS_TOLERANCE = 1.25f
        const val DAMAGE_TO_HULL_THRESHOLD = 0.15f // parry if expected to take this*hullPoints within TIME_TO_IMPACT_BUFFER
    }


    private var shipOpt: ShipAPI? = null
    private var systemOpt: ShipSystemAPI? = null
    private var engineOpt: CombatEngineAPI? = null

    //                        WeaponGroupAPI Group = FIGHTER.getWeaponGroupFor(weapon);

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.shipOpt = ship
        this.systemOpt = system
        this.engineOpt = engine
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val ship = shipOpt ?: return
        val sys = (if (systemOpt?.id == "svc_dash") systemOpt else ship.phaseCloak) ?: return// systemOpt ?: return
        if (!sys.isUsable()) return

        val projectiles = CombatUtils.getProjectilesWithinRange(ship.location, ship.collisionRadius + PROJECTILE_SCAN_RANGE)
        val missiles = CombatUtils.getMissilesWithinRange(ship.location, ship.collisionRadius + PROJECTILE_SCAN_RANGE)
        val ships = CombatUtils.getShipsWithinRange(ship.location, 50f + ship.collisionRadius)
        val enemies = ship.getNearbyEnemies(ship.collisionRadius + 100)
        Global.getLogger(this::class.java).info(enemies)
        var threat = 0f
        var nearShips = 0f



        (projectiles + missiles).filter {
            it.owner != ship.originalOwner
        }.filter {
            CollisionUtils.getCollides(
                it.location, it.location + it.velocity, ship.location,
                ship.collisionRadius + COLLISION_RADIUS_TOLERANCE
            )
        }.filter {
            (ship.location - it.location).length() <= it.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius
        }.forEach {
            threat += it.damageAmount
        }

        (ships).filter {
            it.owner != ship.originalOwner
        }.filter {
            CollisionUtils.getCollides(
                it.location, it.location + it.velocity, ship.location,
                ship.collisionRadius + COLLISION_RADIUS_TOLERANCE
            )
        }.filter {
            (ship.location - it.location).length() <= it.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius
        }.forEach {
            nearShips += it.collisionRadius
        }

//        (collision).filter {
//            it.owner != ship.originalOwner
//        }.filter {
//            CollisionUtils.getCollides(it.location, it.location + it.velocity, ship.location,
//                ship.collisionRadius + COLLISION_RADIUS_TOLERANCE)
//        } .filter {
//            (ship.location - it.location).length() <= it.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius
//        }.forEach {
//            threat += it.collisionRadius
//        }

            if ((sys.ammo.toFloat() * threat > DAMAGE_TO_HULL_THRESHOLD * ship.hitpoints)
                && enemies.isEmpty()

            ) {

                sys.forceState(ShipSystemAPI.SystemState.IN, 0f)
                sys.ammo -= 1
            }

    }
}