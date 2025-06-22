package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.estimateDamageToBeTaken

class NervousShuntAI: ShipSystemAIScript {

    companion object{
        const val SCAN_RANGE = 200f
        const val MAX_HULL_LEVEL_FOR_ACTIVATION = 0.25f
    }

    private var engine: CombatEngineAPI? = null
    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.engine = engine
        this.ship = ship
        this.system = system
    }

    /**
     * activate system if hull level below threshold and incoming damage in next ~1s enough to get killed
     */
    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        ship?.run {
            if(hullLevel > MAX_HULL_LEVEL_FOR_ACTIVATION) return
            if(system?.state != ShipSystemAPI.SystemState.IDLE) return
            if(estimateDamageToBeTaken() > hitpoints) useSystem()
        }
    }
}