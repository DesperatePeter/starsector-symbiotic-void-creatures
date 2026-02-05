package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import org.lwjgl.util.vector.Vector2f

class SpookyActionAI: ShipSystemAIScript {
    private var engine: CombatEngineAPI? = null
    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    enum class TYPE{
        Player, Enemy, Ally, Unknown
    }
    private var type: TYPE = TYPE.Unknown
    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.engine = engine
        this.ship = ship
        this.system = system

        type = when{
            ship == engine?.playerShip -> TYPE.Player
            ship?.originalOwner == 1 -> TYPE.Enemy
            ship?.originalOwner == 0 -> TYPE.Ally
            else -> TYPE.Unknown
        }
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        when(type){
            TYPE.Player -> return // Auto-piloted player ship simply doesn't use the system, as opening a GUI doesn't fit the concept of autopilot
            TYPE.Enemy -> ship?.useSystem() // Mastermind should use the system whenever possible, right? Or should we add a range check?
            TYPE.Ally -> ship?.useSystem() // Allied ships should use the system when it is usable
            TYPE.Unknown -> return
        }
    }
}