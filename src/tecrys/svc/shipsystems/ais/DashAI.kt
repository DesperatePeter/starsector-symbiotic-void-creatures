package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.isUsable

class DashAI: ShipSystemAIScript {
    companion object{
        const val TIME_TO_IMPACT_BUFFER = 0.5f
        const val PROJECTILE_SCAN_RANGE = 500f
        const val COLLISION_RADIUS_TOLERANCE = 1.25f
        const val DAMAGE_TO_HULL_THRESHOLD = 0.1f
        const val ENEMY_PROXIMITY_RANGE_SQ = 300f * 300f
    }

    private var shipOpt: ShipAPI? = null
    private var systemOpt: ShipSystemAPI? = null
    private var engineOpt: CombatEngineAPI? = null

    // Throttle the heavy math to 10 checks a second
    private val tracker = IntervalUtil(0.1f, 0.1f)
    // Cache the enemy proximity so blockCommandForOneFrame can be applied between ticks
    private var isEnemyNearby = false

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.shipOpt = ship
        this.systemOpt = system
        this.engineOpt = engine
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val ship = shipOpt ?: return
        val sys = (if (systemOpt?.id == "svc_dash") systemOpt else ship.phaseCloak) ?: return

        // Commands must be blocked every frame. We use the cached boolean for this.
        if (isEnemyNearby) {
            ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
        }

        tracker.advance(amount)
        if (!tracker.intervalElapsed()) return

        // --- Fast Enemy Proximity Check ---
        isEnemyNearby = false
        val allShips = Global.getCombatEngine().ships
        val numAllShips = allShips.size
        for (i in 0 until numAllShips) {
            val s = allShips[i]
            if (s.owner != ship.owner && s.owner != 100 && !s.isHulk) {
                if (MathUtils.getDistanceSquared(ship.location, s.location) < ENEMY_PROXIMITY_RANGE_SQ) {
                    isEnemyNearby = true
                    ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
                    break
                }
            }
        }

        if (!sys.isUsable()) return

        val checkRange = ship.collisionRadius + PROJECTILE_SCAN_RANGE
        val colTolerance = ship.collisionRadius + COLLISION_RADIUS_TOLERANCE
        var threat = 0f
        var nearShips = 0f

        // --- Projectile Threat Evaluation ---
        val projectiles = CombatUtils.getProjectilesWithinRange(ship.location, checkRange)
        val numProjectiles = projectiles.size
        for (i in 0 until numProjectiles) {
            val p = projectiles[i]
            if (p.owner == ship.originalOwner) continue

            if (CollisionUtils.getCollides(p.location, Vector2f(p.location.x + p.velocity.x, p.location.y + p.velocity.y), ship.location, colTolerance)) {
                val dist = (Vector2f(ship.location.x - p.location.x, ship.location.y - p.location.y)).length()
                if (dist <= p.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius) {
                    threat += p.damageAmount
                }
            }
        }

        // --- Missile Threat Evaluation ---
        val missiles = CombatUtils.getMissilesWithinRange(ship.location, checkRange)
        val numMissiles = missiles.size
        for (i in 0 until numMissiles) {
            val m = missiles[i]
            if (m.owner == ship.originalOwner) continue

            if (CollisionUtils.getCollides(m.location, Vector2f(m.location.x + m.velocity.x, m.location.y + m.velocity.y), ship.location, colTolerance)) {
                val dist = (Vector2f(ship.location.x - m.location.x, ship.location.y - m.location.y)).length()
                if (dist <= m.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius) {
                    threat += m.damageAmount
                }
            }
        }

        // --- Ship Collision Evaluation ---
        val ships = CombatUtils.getShipsWithinRange(ship.location, 270f)
        val numShips = ships.size
        for (i in 0 until numShips) {
            val s = ships[i]
            if (s.owner == ship.originalOwner) continue

            if (CollisionUtils.getCollides(s.location, Vector2f(s.location.x + s.velocity.x, s.location.y + s.velocity.y), ship.location, colTolerance)) {
                val dist = (Vector2f(ship.location.x - s.location.x, ship.location.y - s.location.y)).length()
                if (dist <= s.velocity.length() * TIME_TO_IMPACT_BUFFER + ship.collisionRadius) {
                    nearShips += s.collisionRadius
                }
            }
        }

        // --- Execute Dash ---
        if ((sys.ammo.toFloat() * threat > DAMAGE_TO_HULL_THRESHOLD * ship.hitpoints)) {
            sys.forceState(ShipSystemAPI.SystemState.IN, 0f)
            sys.ammo -= 1
        }
    }
}