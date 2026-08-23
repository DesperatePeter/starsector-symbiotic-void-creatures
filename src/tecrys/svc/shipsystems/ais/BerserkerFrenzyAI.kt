package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.getEffectiveShipTarget

class BerserkerFrenzyAI: ShipSystemAIScript {
    companion object{
        const val NUMBER_WEAPONS_TO_CONSIDER = 4
    }

    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    // Cache the top weapons so we don't sort every frame
    private var topWeapons: List<WeaponAPI> = emptyList()

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
        this.system = system

        // Sort and store the top weapons exactly once when the system initializes
        topWeapons = ship?.allWeapons
            ?.sortedByDescending { it.damage.damage }
            ?.take(NUMBER_WEAPONS_TO_CONSIDER) ?: emptyList()
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val tgt = target ?: return
        ship?.run {
            var shouldActivate = true
            for (w in topWeapons) {
                tgt.exactBounds?.update(tgt.location, tgt.facing)
                val distToTarget = (Vector2f(w.location.x - CollisionUtils.getNearestPointOnBounds(w.location, tgt).x, w.location.y - CollisionUtils.getNearestPointOnBounds(w.location, tgt).y)).length()
                if (w.range <= distToTarget) {
                    shouldActivate = false
                    break
                }
            }
            if(shouldActivate) {
                useSystem()
            }
        }
    }
}