package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.CollisionUtils
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.getMissilesWithinRangeArc
import tecrys.svc.utils.getProjectilesWithinRangeArc
import tecrys.svc.utils.isUsable

class ParryAI: ShipSystemAIScript {
    companion object{
        const val TIME_TO_IMPACT_BUFFER = 0.5f
        const val PROJECTILE_SCAN_RANGE = 400f
        const val COLLISION_RADIUS_TOLERANCE = 1.25f
        const val DAMAGE_TO_HULL_THRESHOLD = 0.2f
    }

    private var shipOpt: ShipAPI? = null
    private var systemOpt: ShipSystemAPI? = null
    private var engineOpt: CombatEngineAPI? = null

    // Throttle the AI so it only scans the grid 10 times a second instead of 60
    private val tracker = IntervalUtil(0.1f, 0.1f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.shipOpt = ship
        this.systemOpt = system
        this.engineOpt = engine
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        tracker.advance(amount)
        if (!tracker.intervalElapsed()) return

        val ship = shipOpt ?: return
        val sys = (if(systemOpt?.id == "parry") systemOpt else ship.phaseCloak) ?: return
        if(!sys.isUsable()) return

        val checkRange = ship.collisionRadius + PROJECTILE_SCAN_RANGE
        val colTolerance = ship.collisionRadius + COLLISION_RADIUS_TOLERANCE
        var threat = 0f

        // Check projectiles directly without concatenating lists
        val projectiles = getProjectilesWithinRangeArc(ship.location, checkRange, 180f, ship.facing)
        for (i in 0 until projectiles.size) {
            val p = projectiles[i]
            if (p.owner == ship.originalOwner) continue
            if (CollisionUtils.getCollides(p.location, Vector2f(p.location.x + p.velocity.x, p.location.y + p.velocity.y), ship.location, colTolerance)) {
                val dist = (Vector2f(ship.location.x - p.location.x, ship.location.y - p.location.y)).length()
                if (dist <= p.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius) {
                    threat += p.damageAmount
                }
            }
        }

        // Check missiles directly
        val missiles = getMissilesWithinRangeArc(ship.location, checkRange, 180f, ship.facing)
        for (i in 0 until missiles.size) {
            val m = missiles[i]
            if (m.owner == ship.originalOwner) continue
            if (CollisionUtils.getCollides(m.location, Vector2f(m.location.x + m.velocity.x, m.location.y + m.velocity.y), ship.location, colTolerance)) {
                val dist = (Vector2f(ship.location.x - m.location.x, ship.location.y - m.location.y)).length()
                if (dist <= m.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius) {
                    threat += m.damageAmount
                }
            }
        }

        if(sys.ammo.toFloat() * threat > DAMAGE_TO_HULL_THRESHOLD * ship.hitpoints){
            sys.forceState(ShipSystemAPI.SystemState.IN, 0f)
            sys.ammo -= 1
        }
    }
}