package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f

class BerserkerFrenzyAI: ShipSystemAIScript {
    private var engine: CombatEngineAPI? = null
    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.engine = engine
        this.ship = ship
        this.system = system
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        ship?.run {

        }
    }
}